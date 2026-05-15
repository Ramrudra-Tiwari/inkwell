with open('d:/inkwell/auth-service/auth-service/src/main/resources/application-local.yml', 'r') as f:
    for line in f:
        if 'client-id:' in line:
            print(f"ID: |{line.split(':', 1)[1].strip()}|")
        if 'client-secret:' in line:
            print(f"SECRET: |{line.split(':', 1)[1].strip()}|")
        if 'redirect-uri:' in line:
            print(f"URI: |{line.split(':', 1)[1].strip()}|")
