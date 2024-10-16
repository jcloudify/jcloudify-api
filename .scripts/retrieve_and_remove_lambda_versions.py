import boto3


def get_lambda_client():
    return boto3.client("lambda")


def get_all_lambda_functions():
    """Retrieve all Lambda functions in the account."""
    client = get_lambda_client()
    paginator = client.get_paginator("list_functions")
    functions = []

    for page in paginator.paginate():
        functions.extend(page["Functions"])

    return functions


def get_lambda_versions(function_name, client):
    """Retrieve all versions of a Lambda function."""
    paginator = client.get_paginator("list_versions_by_function")
    versions = []

    for page in paginator.paginate(FunctionName=function_name):
        versions.extend(page["Versions"])

    return versions


def is_alias_referenced(function_name, version, client):
    response = client.list_aliases(FunctionName=function_name)
    for alias in response["Aliases"]:
        if alias["FunctionVersion"] == version:
            print(f"Alias {alias['Name']} points to version {version}")
            return True
    return False


def delete_old_lambda_versions(function_name):
    client = get_lambda_client()
    """Delete old versions of the Lambda function, keeping only the latest one."""
    versions = get_lambda_versions(function_name, client)

    old_versions = sorted(
        [v for v in versions if v["Version"] != "$LATEST"],
        key=lambda x: int(x["Version"]),
    )

    if len(old_versions) > 1:
        old_versions.pop()

        for version in old_versions:
            version_number = version["Version"]
            if not is_alias_referenced(function_name, version_number, client):
                print(
                    f"Deleting version {version_number} of Lambda function {function_name}"
                )
                client.delete_function(
                    FunctionName=function_name, Qualifier=version_number
                )


def main():
    functions = get_all_lambda_functions()

    for function in functions:
        function_name = function["FunctionName"]
        print(f"Processing Lambda function: {function_name}")
        delete_old_lambda_versions(function_name)


if __name__ == "__main__":
    main()
