#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

echo "🚀 Starting Auth Service Kubernetes Deployment..."

# 1. Apply Configuration and Secrets
echo "📦 Applying Secrets and ConfigMaps..."
kubectl apply -f k8s/auth-secrets.yaml
kubectl apply -f k8s/app-config.yaml

# 2. Apply Infrastructure (DB and Cache)
echo "💾 Starting Database and Redis..."
kubectl apply -f k8s/db.yaml
kubectl apply -f k8s/redis.yaml

# 3. Apply Services (Backend and Frontend)
echo "⚙️  Starting Backend and Frontend..."
kubectl apply -f k8s/backend.yaml
kubectl apply -f k8s/frontend.yaml

# 4. Wait for the Frontend to be ready
echo "⏳ Waiting for the Frontend pod to be ready..."
kubectl wait --for=condition=ready pod -l app=auth-frontend --timeout=90s

echo "✅ Deployment Complete!"
echo "-------------------------------------------------------"
echo "🌐 Accessing the App:"
echo "The system is running inside the cluster."
echo "I am now opening a tunnel to http://localhost:8080"
echo "(Press Ctrl+C to stop the tunnel and exit)"
echo "-------------------------------------------------------"

# 5. Start Port-Forwarding automatically
kubectl port-forward svc/frontend 8080:80