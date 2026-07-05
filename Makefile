.PHONY: infra up up-real frontend stop

# Load .env file if it exists
-include .env
export

## Start only MySQL + Redis (needed by both backend modes)
infra:
	docker compose up -d mysql redis

## Start backend with stub fare fetcher (ignores SERPAPI_KEY even if set in .env)
up: infra
	SERPAPI_KEY= mvn spring-boot:run

## Start backend with real SerpApi fare fetcher (reads SERPAPI_KEY from .env)
up-real: infra
	mvn spring-boot:run

## Start the frontend dev server
frontend:
	cd frontend && npm install && npm run dev

## Stop all Docker services
stop:
	docker compose down
