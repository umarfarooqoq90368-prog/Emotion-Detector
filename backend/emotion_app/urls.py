from django.urls import path
from .views import (
    RegisterUserView,
    ForgotPasswordView,
    UserProfileView,
    AnalyzeEmotionView,
    AnalyticsDashboardView,
)

urlpatterns = [
    # Authorization pathways
    path('register/', RegisterUserView.as_view(), name='auth_register'),
    path('forgot-password/', ForgotPasswordView.as_view(), name='auth_forgot'),
    
    # Profile & Settings
    path('profile/', UserProfileView.as_view(), name='profile_detail'),
    
    # Core Engine Features
    path('analyze/', AnalyzeEmotionView.as_view(), name='engine_analyze'),
    path('analytics/', AnalyticsDashboardView.as_view(), name='engine_analytics'),
]
