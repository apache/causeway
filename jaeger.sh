docker kill jaeger 2>&1 >/dev/null
docker rm jaeger 2>&1 >/dev/null
docker run -d --name jaeger \
  -p 16686:16686 \
  -p 4317:4317 \
  -p 4318:4318 \
  jaegertracing/all-in-one:latest

echo "http://localhost:16686"