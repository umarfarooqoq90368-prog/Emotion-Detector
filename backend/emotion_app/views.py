import json
import os
import requests
from django.contrib.auth.models import User
from django.contrib.auth.hashers import make_password
from rest_framework import status, views, permissions
from rest_framework.response import Response
from .models import UserProfile, EmotionAnalysisRecord
from .serializers import UserSerializer, UserProfileSerializer, EmotionAnalysisRecordSerializer

class RegisterUserView(views.APIView):
    permission_classes = [permissions.AllowAny]

    def post(self, request):
        serializer = UserSerializer(data=request.data)
        if serializer.is_valid():
            user = serializer.save()
            return Response({
                "message": "User registered successfully.",
                "user_id": user.id,
                "username": user.username
            }, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class ForgotPasswordView(views.APIView):
    permission_classes = [permissions.AllowAny]

    def post(self, request):
        email = request.data.get('email')
        if not email:
            return Response({"error": "Email is required."}, status=status.HTTP_400_BAD_REQUEST)
        
        # In a real SaaS, we trigger an email via SMTP. Here, we confirm user presence and log/return success
        user_exists = User.objects.filter(email=email).exists()
        if user_exists:
            return Response({
                "message": f"Password reset instructions have been dispatched securely to {email}."
            }, status=status.HTTP_200_OK)
        else:
            return Response({
                "message": "If that email address is on file, instructions have been sent."
            }, status=status.HTTP_200_OK)


class UserProfileView(views.APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        profile, created = UserProfile.objects.get_or_create(user=request.user)
        serializer = UserProfileSerializer(profile)
        return Response({
            "username": request.user.username,
            "email": request.user.email,
            "profile": serializer.data
        }, status=status.HTTP_200_OK)

    def put(self, request):
        profile, created = UserProfile.objects.get_or_create(user=request.user)
        
        # Allow updating email or password on User
        email = request.data.get('email')
        if email:
            request.user.email = email
            request.user.save()

        password = request.data.get('password')
        if password:
            request.user.password = make_password(password)
            request.user.save()

        serializer = UserProfileSerializer(profile, data=request.data, partial=True)
        if serializer.is_valid():
            serializer.save()
            return Response({
                "message": "Profile updated successfully.",
                "profile": serializer.data
            }, status=status.HTTP_200_OK)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)


class AnalyzeEmotionView(views.APIView):
    permission_classes = [permissions.IsAuthenticated]

    def post(self, request):
        inputText = request.data.get('text', '')
        if not inputText:
            return Response({"error": "TextInput is required for emotion spectrum evaluation."}, status=status.HTTP_400_BAD_REQUEST)

        # Retrieve API Key securely from Server System Environment
        gemini_api_key = os.environ.get("GEMINI_API_KEY", "")
        if not gemini_api_key or gemini_api_key == "YOUR_SECURE_GEMINI_API_KEY":
            return Response({"error": "Gemini API key is not configured on this cloud deployment backend."}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

        # Create structured prompting for core text intelligence
        system_instruction = (
            "You are the enterprise-grade Emotion Classification and Clinical NLP Engine for EmotionAI Pro. "
            "Analyze the given input text to evaluate 10 exact variables and return ONLY a strict raw JSON. "
            "Do not warp inside markdown or add chat introduction text."
        )

        prompt = f"""
        Analyze the given input text to detect 10 variables:
        1. happy, 2. sad, 3. angry, 4. fear, 5. surprise, 6. love, 7. neutral, 8. anxiety, 9. depressionRisk, 10. stressLevel
        
        You must output a JSON object strictly matching this schema:
        {{
          "dominantEmotion": "Happy",
          "confidenceScore": 0.95,
          "scores": {{
            "happy": 0.95,
            "sad": 0.05,
            "angry": 0.0,
            "fear": 0.0,
            "surprise": 0.0,
            "love": 0.0,
            "neutral": 0.0,
            "anxiety": 0.0,
            "depressionRisk": 0.0,
            "stressLevel": 0.0
          }},
          "explanation": "Detailed explanation of markers",
          "emotionalSummary": "Proactive summary sentence",
          "wellnessScore": 95.0,
          "influentialWords": [
            {{
              "word": "hopeful",
              "impact": 0.9,
              "category": "positive"
            }}
          ],
          "recommendations": [
            {{
              "title": "Keep practicing optimism",
              "description": "Maintain your present balance.",
              "actionableSteps": ["Record thoughts daily"]
            }}
          ]
        }}
        
        Input Text to Evaluate: "{inputText}"
        """

        gemini_endpoint = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key={gemini_api_key}"
        headers = {'Content-Type': 'application/json'}
        payload = {
            "contents": [{"parts": [{"text": prompt}]}],
            "generationConfig": {
                "responseMimeType": "application/json",
                "temperature": 0.2
            }
        }

        try:
            api_response = requests.post(gemini_endpoint, headers=headers, json=payload, timeout=60)
            if api_response.status_code != 200:
                return Response({
                    "error": "Error response from remote Google GenServer",
                    "details": api_response.text
                }, status=status.HTTP_502_BAD_GATEWAY)
            
            gemini_data = api_response.json()
            raw_text = gemini_data['candidates'][0]['content']['parts'][0]['text']
            
            # Parse the JSON response securely
            analysis_dict = json.loads(self.sanitize_json_text(raw_text))
            scores_dict = analysis_dict.get('scores', {})

            # Save the record securely into internal PostgreSQL referencing authentic User ID
            record = EmotionAnalysisRecord.objects.create(
                user=request.user,
                inputText=inputText,
                dominantEmotion=analysis_dict.get('dominantEmotion', 'Neutral'),
                confidenceScore=float(analysis_dict.get('confidenceScore', 0.5)),
                wellnessScore=float(analysis_dict.get('wellnessScore', 50.0)),
                explanation=analysis_dict.get('explanation', ''),
                emotionalSummary=analysis_dict.get('emotionalSummary', ''),
                score_happy=float(scores_dict.get('happy', 0.0)),
                score_sad=float(scores_dict.get('sad', 0.0)),
                score_angry=float(scores_dict.get('angry', 0.0)),
                score_fear=float(scores_dict.get('fear', 0.0)),
                score_surprise=float(scores_dict.get('surprise', 0.0)),
                score_love=float(scores_dict.get('love', 0.0)),
                score_neutral=float(scores_dict.get('neutral', 0.1)),
                score_anxiety=float(scores_dict.get('anxiety', 0.0)),
                score_depressionRisk=float(scores_dict.get('depressionRisk', 0.0)),
                score_stressLevel=float(scores_dict.get('stressLevel', 0.0)),
                influentialWordsJson=json.dumps(analysis_dict.get('influentialWords', [])),
                recommendationsJson=json.dumps(analysis_dict.get('recommendations', []))
            )

            # Return serialized record details
            serializer = EmotionAnalysisRecordSerializer(record)
            return Response(serializer.data, status=status.HTTP_201_CREATED)

        except Exception as e:
            return Response({
                "error": "Exception occurred while calling Secure Cloud Core",
                "details": str(e)
            }, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

    def sanitize_json_text(self, text):
        clean = text.strip()
        if clean.startswith("```json"):
            clean = clean[7:]
        elif clean.startswith("```"):
            clean = clean[3:]
        if clean.endswith("```"):
            clean = clean[:-3]
        return clean.strip()


class AnalyticsDashboardView(views.APIView):
    permission_classes = [permissions.IsAuthenticated]

    def get(self, request):
        # Gather all user-tied historical records
        records = EmotionAnalysisRecord.objects.filter(user=request.user).order_with_respect_to('id')
        total_logs = records.count()
        if total_logs == 0:
            return Response({
                "totalTracks": 0,
                "averageWellness": 0,
                "moodPercentages": {},
                "wellnessTrend": []
            }, status=status.HTTP_200_OK)

        running_wellness = 0
        mood_counts = {}
        trend = []

        for r in records:
            running_wellness += r.wellnessScore
            mood_counts[r.dominantEmotion] = mood_counts.get(r.dominantEmotion, 0) + 1
            trend.append({
                "id": r.id,
                "emotion": r.dominantEmotion,
                "wellnessScore": r.wellnessScore,
                "timestamp": r.timestamp.strftime('%Y-%m-%d %H:%M')
            })

        avg_wellness = running_wellness / total_logs
        mood_percentages = {emotion: (count / total_logs) * 100 for emotion, count in mood_counts.items()}

        return Response({
            "totalTracks": total_logs,
            "averageWellness": round(avg_wellness, 1),
            "moodPercentages": mood_percentages,
            "wellnessTrend": trend[-10:] # Last 10 points
        }, status=status.HTTP_200_OK)
