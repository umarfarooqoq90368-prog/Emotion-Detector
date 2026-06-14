from rest_framework import serializers
from django.contrib.auth.models import User
from .models import UserProfile, EmotionAnalysisRecord
import json

class UserProfileSerializer(serializers.ModelSerializer):
    class Meta:
        model = UserProfile
        fields = ['fullName', 'avatarUrl', 'isDailyReminderEnabled', 'isMoodReminderEnabled']


class UserSerializer(serializers.ModelSerializer):
    profile = UserProfileSerializer(required=False)

    class Meta:
        model = User
        fields = ['id', 'username', 'email', 'password', 'profile']
        extra_kwargs = {'password': {'write_only': True}}

    def create(self, validated_data):
        profile_data = validated_data.pop('profile', {})
        user = User.objects.create_user(
            username=validated_data['username'],
            email=validated_data.get('email', ''),
            password=validated_data['password']
        )
        # Create user profile
        UserProfile.objects.create(user=user, **profile_data)
        return user


class EmotionAnalysisRecordSerializer(serializers.ModelSerializer):
    class Meta:
        model = EmotionAnalysisRecord
        fields = '__all__'
        read_only_fields = ['id', 'user', 'timestamp']

    def to_representation(self, instance):
        representation = super().to_representation(instance)
        try:
            representation['influentialWords'] = json.loads(instance.influentialWordsJson)
        except Exception:
            representation['influentialWords'] = []
        try:
            representation['recommendations'] = json.loads(instance.recommendationsJson)
        except Exception:
            representation['recommendations'] = []
        return representation
