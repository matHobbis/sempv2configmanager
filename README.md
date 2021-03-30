**Solace SEMP V2 Configuration Manager**

This is a short example of the use of the SEMP V2 interface to extract and apply virtual broker configuration. The configuration is in the form of json files that may be stored and versioned. 

The code makes use of the java library org,json that should be added to your code environment.

The basic use of the code is as follows - 

```
Usage : VirtualBrokerConfiguration get <host> <admin-user> <admin-pass> <vpnname> <filename-prefix> <opaque-data-pwd>

Where : 
host		= hostname:port or hostip:port - only TLS connections are supported so this should
                     directed at the secure SEMP port of the broker
admin-user      = admin username 
admin-pass      = admin password 
vpn-name	= vpn name to get 
filename-prefix = file prefix to use to build the file 
opaque-data-pwd = password used to encyrpt opaque data (passwords) 
 
This process will connect to the message broker and message VPN listed in host and vpn-name using the 
admin-user and admin-pass credentials. The process will extract the configuration of the VPN and its 
child objects and write the configuration using a JSON representation to a file with the name 
obtained from the file prefix, vpn-name and the current timestamp
```  


This will extract the configuration from the given virtual message broker (host + VPN) and provide a json file with the filename given by the prefix and the vpn name. 

A snippet of the returned file is shown below. The configuration information has an additional attributes section that can aid with versioning and also keeps track of the opaque password used to safely retrieve 'write-only' attributes, such as user passwords, and the SEMP version that the configuration is taken from. Attributes extracted via the opaque password can only be applied to the same SEMP version.

```
{
    "attributes": {
        "host": "www.example.com:11943",
        "msgVpnName": "default",
        "opaquePassword": "123123123123123",
        "sempVersion": "2.19",
        "timestamp": "2021-03-30T12:37:13.184905"
    },
    "config": {
        "_msgVpns": [{
            "alias": "",
            "authenticationBasicEnabled": true,
            "authenticationBasicProfileName": "",
            "authenticationBasicRadiusDomain": "",
            ...
            "serviceWebTlsEnabled": true,
            "tlsAllowDowngradeToPlainTextEnabled": false
        }],
        "aclProfiles": [{
            "aclProfileName": "default",
            "clientConnectDefaultAction": "allow",
            "msgVpnName": "default",
            "publishTopicDefaultAction": "allow",
            "subscribeShareNameDefaultAction": "allow",
            "subscribeTopicDefaultAction": "allow"
        }],
        "aclProfiles/default/clientConnectExceptions": [],
        "aclProfiles/default/publishTopicExceptions": [],
        "aclProfiles/default/subscribeShareNameExceptions": [],
        "aclProfiles/default/subscribeTopicExceptions": [],
        "authenticationOauthProviders": [],
        "authorizationGroups": [],
        "bridges": [],
        "clientProfiles": [{
            "allowBridgeConnectionsEnabled": true,
            "allowCutThroughForwardingEnabled": true,
            ...
            "tcpMaxWindowSize": 256,
            "tlsAllowDowngradeToPlainTextEnabled": true
        }],
        "clientUsernames": [{
            "aclProfileName": "default",
            "clientProfileName": "default",
            "clientUsername": "default",
            "enabled": true,
            "guaranteedEndpointPermissionOverrideEnabled": false,
            "msgVpnName": "default",
            "password": "",
            "subscriptionManagerEnabled": false
        }],
        "distributedCaches": [],
        "dmrBridges": [],
        "jndiConnectionFactories": [{
            "allowDuplicateClientIdEnabled": false,
            "clientDescription": "",
            ..
            "transportTcpNoDelayEnabled": true,
            "xaEnabled": true
        }],
        "jndiQueues": [],
        "jndiTopics": [],
        "mqttRetainCaches": [],
        "mqttSessions": [],
        "queueTemplates": [],
        "queues": [
            {
                "accessType": "exclusive",
           	...
                "queueName": "mhobbisQueue",
                "redeliveryEnabled": true,
                "rejectLowPriorityMsgEnabled": false,
                "rejectLowPriorityMsgLimit": 0,
                "rejectMsgToSenderOnDiscardBehavior": "when-queue-enabled",
                "respectMsgPriorityEnabled": false,
                "respectTtlEnabled": false
            },
            {
                "accessType": "exclusive",
                "consumerAckPropagationEnabled": true,
                ...
                "queueName": "q0",
                "redeliveryEnabled": true,
                "rejectLowPriorityMsgEnabled": false,
                "rejectLowPriorityMsgLimit": 0,
                "rejectMsgToSenderOnDiscardBehavior": "when-queue-enabled",
                "respectMsgPriorityEnabled": false,
                "respectTtlEnabled": false
            }
        ]
        "queues/q10/subscriptions": [],
        "queues/q11/subscriptions": [],
        "queues/q12/subscriptions": [],
        "queues/q13/subscriptions": [],
        "queues/q14/subscriptions": [],
        "queues/q2/subscriptions": [],
        "queues/q3/subscriptions": [],
        "queues/q4/subscriptions": [],
        "queues/q5/subscriptions": [],
        "queues/q6/subscriptions": [],
        "queues/q7/subscriptions": [],
        "queues/q8/subscriptions": [],
        "queues/q9/subscriptions": [],
        "queues/test/subscriptions": [],
        "replayLogs": [],
        "replicatedTopics": [],
        "restDeliveryPoints": [],
        "sequencedTopics": [],
        "topicEndpointTemplates": [],
        "topicEndpoints": []
    }
}
```


The extracted configutaion json file can be applied by rerunning with the 'put' command and specifying the broker to target. Changes to attributes such as the VPN name should be carried out before reapplying the file.

```
Usage : VirtualBrokerConfiguration put <host> <admin-user> <admin-pass> <filename> <opaque-data-pwd>

Where : 
host		= hostname:port or hostip:port 
admin-user      = admin username 
admin-pass      = admin password 
filename        = filename that holds the json configuration for the VPN 
opaque-data-pwd = password used to encyrpt opaque data (passwords) 
 
This process will connect to the message broker and message VPN listed in host and vpn-name using the 
admin-user and admin-pass credentials. The process will extract the configuration from the given file  
and apply the config to the vpn on the broker. 
```
