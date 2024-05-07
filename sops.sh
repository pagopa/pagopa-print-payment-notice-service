#!/bin/bash

#set -x

action=$1
env=$2
file=$3

azure_kv_url=$(az keyvault key show --name pagopa-"$env"-printit-sops-key --vault-name pagopa-${env:0:1}-itn-printit-kv --query key.kid | sed 's/"//g')

if [ "$action" == "e" ]; then
  sops --encrypt --azure-kv "$azure_kv_url" --input-type dotenv --output-type  dotenv ./"$file" > ./"$file".encrypted
fi;

if [ "$action" == "d" ]; then
  sops --decrypt --azure-kv "$azure_kv_url" --input-type dotenv --output-type dotenv ./"$file".encrypted > ./"$file"
fi;


echo 'done'
