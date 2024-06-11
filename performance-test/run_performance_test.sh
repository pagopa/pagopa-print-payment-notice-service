# sh run_performance_test.sh <local|dev|uat|prod> <load|stress|spike|soak|...> <script-name> <db-name> <apim_subscription_key>

ENVIRONMENT=$1
TYPE=$2
SCRIPT=$3
DB_NAME=$4
OCP_APIM_SUBSCRIPTION_KEY=$5
TEMPLATE_ID=$6
PROCESS_TIME=$7
NUMBER_OF_MASSIVE_ELEMENTS=$8

if [ -z "$ENVIRONMENT" ]
then
  echo "No env specified: sh run_performance_test.sh <local|dev|uat|prod> <load|stress|spike|soak|...> <script-name> <db-name> <apim_subscription_key> <template_id> <process_time> <number_of_elements>"
  exit 1
fi

if [ -z "$TYPE" ]
then
  echo "No test type specified: sh run_performance_test.sh <local|dev|uat|prod> <load|stress|spike|soak|...> <script-name> <db-name> <apim_subscription_key> <template_id> <process_time> <number_of_elements>"
  exit 1
fi
if [ -z "$SCRIPT" ]
then
  echo "No script name specified: sh run_performance_test.sh <local|dev|uat|prod> <load|stress|spike|soak|...> <script-name> <db-name> <apim_subscription_key> <template_id> <process_time> <number_of_elements>"
  exit 1
fi

export env=${ENVIRONMENT}
export type=${TYPE}
export script=${SCRIPT}
export db_name=${DB_NAME}
export ocp_apim_subscription_key=${OCP_APIM_SUBSCRIPTION_KEY}
export template_id=${TEMPLATE_ID}
export process_time=${PROCESS_TIME}
export number_of_massive_elements=${NUMBER_OF_MASSIVE_ELEMENTS}

stack_name=$(cd .. && basename "$PWD")
docker compose -p "${stack_name}-k6" up -d --remove-orphans --force-recreate --build
docker logs -f k6
docker stop nginx
