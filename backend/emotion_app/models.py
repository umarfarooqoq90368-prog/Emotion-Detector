from django.db import models
from django.contrib.auth.models import User

class UserProfile(models.Model):
    user = models.OneToOneField(User, on_delete=models.CASCADE, related_name='profile')
    fullName = models.CharField(max_length=255, blank=True)
    avatarUrl = models.URLField(max_length=1000, blank=True, null=True, default="https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png")
    isDailyReminderEnabled = models.BooleanField(default=True)
    isMoodReminderEnabled = models.BooleanField(default=True)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"Profile of {self.user.username}"


class EmotionAnalysisRecord(models.Model):
    user = models.ForeignKey(User, on_delete=models.CASCADE, related_name='emotion_records')
    inputText = models.TextField()
    timestamp = models.DateTimeField(auto_now_add=True)
    
    # Core outputs
    dominantEmotion = models.CharField(max_length=100)
    confidenceScore = models.FloatField()
    wellnessScore = models.FloatField()
    explanation = models.TextField()
    emotionalSummary = models.TextField()
    
    # Detailed sub-scores
    score_happy = models.FloatField(default=0.0)
    score_sad = models.FloatField(default=0.0)
    score_angry = models.FloatField(default=0.0)
    score_fear = models.FloatField(default=0.0)
    score_surprise = models.FloatField(default=0.0)
    score_love = models.FloatField(default=0.0)
    score_neutral = models.FloatField(default=0.0)
    score_anxiety = models.FloatField(default=0.0)
    score_depressionRisk = models.FloatField(default=0.0)
    score_stressLevel = models.FloatField(default=0.0)
    
    # JSON or serialized structures representing arrays
    influentialWordsJson = models.TextField(default="[]")
    recommendationsJson = models.TextField(default="[]")

    def __str__(self):
        return f"{self.user.username} - {self.dominantEmotion} ({self.timestamp.strftime('%Y-%m-%d %H:%M')})"
