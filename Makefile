.PHONY: infra up up-real frontend stop

# Load .env file if it exists
-include .env
export

MVN := $(shell which mvn 2>/dev/null || find ~/.m2/wrapper/dists -name mvn -type f 2>/dev/null | sort -r | head -1)

## Start only MySQL + Redis (needed by both backend modes)
infra:
	docker compose up -d mysql redis

## Start backend with stub fare fetcher (ignores SERPAPI_KEY even if set in .env)
up: infra
	SERPAPI_KEY= $(MVN) spring-boot:run

## Start backend with real SerpApi fare fetcher (reads SERPAPI_KEY from .env)
up-real: infra
	$(MVN) spring-boot:run

## Start the frontend dev server (requires Node 20+; activates via nvm if available)
frontend:
	bash -c 'export NVM_DIR="$$HOME/.nvm" && [ -s "$$NVM_DIR/nvm.sh" ] && . "$$NVM_DIR/nvm.sh" && nvm use 20; cd frontend && npm install && npm run dev'

## Stop all Docker services
stop:
	docker compose down
