# Print Payment Notice - Service

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=pagopa_pagopa-print-payment-notice-service&metric=alert_status)](https://sonarcloud.io/dashboard?id=pagopa_pagopa-print-payment-notice-service)
[![Integration Tests](https://github.com/pagopa/pagopa-print-payment-notice-service/actions/workflows/integration_test.yml/badge.svg?branch=main)](https://github.com/pagopa/pagopa-print-payment-notice-service/actions/workflows/integration_test.yml)

This µ-service exposes the REST APIs and handles the requests for "Stampa Avvisi".

- [Print Payment Notice - Service](#print-payment-notice---service)
    * [Api Documentation 📖](#api-documentation---)
    * [Technology Stack](#technology-stack)
    * [Start Project Locally 🚀](#start-project-locally---)
        + [Prerequisites](#prerequisites)
        + [Run docker container](#run-docker-container)
    * [Develop Locally 💻](#develop-locally---)
        + [Prerequisites](#prerequisites-1)
        + [Run the project](#run-the-project)
        + [Spring Profiles](#spring-profiles)
        + [Testing 🧪](#testing---)
            - [Unit testing](#unit-testing)
            - [Integration testing](#integration-testing)
            - [Performance testing](#performance-testing)
    * [Contributors 👥](#contributors---)
        + [Maintainers](#maintainers)

---

## Api Documentation 📖

See
the [OpenApi 3 here.](https://editor.swagger.io/?url=https://raw.githubusercontent.com/pagopa/pagopa-print-payment-notice-service/main/openapi/openapi.json)

---

## Technology Stack

- Java 17
- Spring Boot 3
- Spring Web
- Hibernate
- JPA
- Mongo
- Spring Cloud

---

## Start Project Locally 🚀

### Prerequisites

- docker

### Run docker container

from `./docker` directory

`sh ./run_docker.sh local`

ℹ️ Note: for PagoPa ACR is required the login `az acr login -n <acr-name>`

---

## Develop Locally 💻

### Prerequisites

- git
- maven
- jdk-17

### Run the project

Start the springboot application with these commands:

``` bash
sh sops.sh dec dev ./src/main/resources/application-local.properties.encrypted
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Spring Profiles

- **local**: to develop locally.
- _default (no profile set)_: The application gets the properties from the environment (for Azure).

### Testing 🧪

#### Unit testing

To run the **Junit** tests:

`mvn clean verify`

#### Integration testing

From `./integration-test/src`

1. `yarn install`
2. `yarn test`

#### Performance testing

install [k6](https://k6.io/) and then from `./performance-test/src`

1. `k6 run --env VARS=local.environment.json --env TEST_TYPE=./test-types/load.json main_scenario.js`

---

## Contributors 👥

Made with ❤️ by PagoPa S.p.A.

### Maintainers

See `CODEOWNERS` file
