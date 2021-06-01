PARENT=$(curl -X GET "https://nexus.incode.work/service/rest/v1/search?sort=version&repository=nightly-builds&group=org.apache.isis.app&name=isis-app-starter-parent" -H "accept: application/json" -s | jq '.items[0].version' | sed 's/"//g')
echo "latest = $PARENT"
