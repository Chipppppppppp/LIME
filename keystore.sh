#/bin/bash

cd "$(dirname $(readlink -f $0))"

if [[ -f "app/release.jks" && -n "$STORE_PASSWORD" && -n "$KEY_ALIAS" && -n "$KEY_PASSWORD" ]]; then
    echo "Config was already set."
    read -r -p "Do you want to reconfigure? [Y/n]: " CHECK
    case ${CHECK} in
        Y|y|Ｙ|ｙ) echo ;;
        *) exit ;;
    esac
fi

read -r -s -p "Enter KeyStore Password: " STORE_PASSWORD
echo
read -r -p "Enter Key Alias: " KEY_ALIAS
read -r -s -p "Enter Key Password: " KEY_PASSWORD
echo
if [[ ! -f app/release.jks ]]; then
    read -r -p "Where is KeyStore?: " STORE_FILE
    STORE_FILE="${STORE_FILE/#\~/$HOME}"
    if [[ ! -f "$STORE_FILE" ]]; then
        echo "File not found: ${STORE_FILE}"
        echo "Please copy the keystore file yourself to app/release.jks."
    else
        cp -f "${STORE_FILE}" "app/release.jks"
    fi
fi
export STORE_PASSWORD
export KEY_ALIAS
export KEY_PASSWORD
