docker buildx create --name multiarch-builder --use
docker buildx inspect --bootstrap

docker buildx build \
  --platform linux/amd64,linux/arm64 \
  -t noahh99/beach-bunny-april-fools:1.0.1 \
  -t noahh99/beach-bunny-april-fools:latest \
  --push .
