# 🚀 Kafka Setup - Event Platform

## Overview
This setup uses Apache Kafka for real-time event streaming in your Event Platform application. Kafka enables:
- **Real-time notifications** (email/SMS for booking confirmations)
- **Analytics & reporting** (track events across the platform)
- **Decoupled microservices** (separate notification logic from core booking logic)

## Prerequisites
Make sure you have Docker and Docker Compose installed:
- [Install Docker Desktop](https://www.docker.com/products/docker-desktop)

---

## 📋 Quick Start (3 Steps)

### Step 1: Start Kafka & ZooKeeper
```bash
# Navigate to docker folder
cd docker

# Start services in background
docker-compose up -d

# Wait ~15 seconds for topics to auto-create
```

### Step 2: Verify Kafka is Running
```bash
# Check if containers are running
docker-compose ps

# Expected output:
# NAME                    STATUS
# event-platform-zookeeper Up
# event-platform-kafka     Up
```

### Step 3: Run Your Application
```bash
cd ..  # Go back to project root
mvn spring-boot:run  # Or run from IDE
```

---

## 🎯 What Happens After Kafka Starts?

Once Kafka is running, your application will:
1. ✅ Connect to `localhost:9092` automatically
2. ✅ Create topics like "bookings" on first use
3. ✅ Publish booking events (confirmed/cancelled) asynchronously
4. ✅ Process events in consumers for notifications/analytics

---

## 🛑 Stop Kafka When Done

```bash
cd docker
docker-compose down  # Stops all services
```

---

## 🔧 Troubleshooting

### Can't connect to Kafka?
```bash
# Check if Kafka is actually running
docker-compose ps

# If not running, start again
docker-compose up -d

# Wait 15-30 seconds before connecting
```

### Topic doesn't exist?
Kafka will auto-create topics on first use. To manually check:
```bash
# Connect to Kafka CLI (optional)
docker exec event-platform-kafka kafka-topics --bootstrap-server localhost:9092 --list
```

---

## 📖 Learning Resources

- [Spring Kafka Docs](https://docs.spring.io/spring-kafka/reference/html/)
- [Apache Kafka Tutorial](https://kafka.apache.org/tutorials/)
- [Understanding Event Sourcing](https://spring.io/guides/gs/event-sourcing/)

---

## ✅ Next Steps After Kafka is Running

1. Run your tests (with Kafka enabled)
2. Integrate email/SMS services in the consumer placeholders
3. Build analytics dashboards to visualize booking events
4. Scale notification logic independently!

---

**Status**: Ready for development 🚀