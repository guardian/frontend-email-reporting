#!/usr/bin/env bash


read -r -d '' header << EOM
{
  "email-send-report-TEST": [
EOM

body=$(for i in `seq 25 1`; do

timer="-""$i""H"
dater=$(date -v $timer -u +"%Y-%m-%dT%H:00:00Z")
opens=$(jot -r 1  1 80)
clicks=$(jot -r 1  1 80)


cat << EOM
    {
    "PutRequest": {
          "Item": {
            "listId": {"S": "1506"},
            "dateTime": { "S": "$dater"  },
            "SendDate" : { "S" : "2015-12-07 01:23:00" },
            "FromAddress" : { "S" : "info@mail.theguardian.com" },
            "FromName" : { "S" : "The Guardian" },
            "Duplicates" : { "N" : "0" },
            "InvalidAddresses" : { "N" : "0" },
            "ExistingUndeliverables" : { "N" : "4" },
            "ExistingUnsubscribes" : { "N" : "0" },
            "HardBounces" : { "N" : "6" },
            "SoftBounces" : { "N" : "0" },
            "OtherBounces" : { "N" : "0" },
            "ForwardedEmails" : { "N" : "0" },
            "UniqueClicks" : { "N" : "$clicks" },
            "UniqueOpens" : { "N" : "$opens" },
            "NumberSent" : { "N" : "80" },
            "NumberDelivered" : { "N" : "74" },
            "Unsubscribes" : { "N" : "0" },
            "MissingAddresses" : { "N" : "0" },
            "Subject" : { "S" : "The Guardian today: " },
            "PreviewURL" : { "S" : "https://members.exacttarget.com/Integration/EmailPreview.aspx?pid=ffcf14&mid=fe961570706d047f7d&eid=fef61679766c07" },
            "SentDate" : { "S" : "2015-12-07 01:24:00" },
            "EmailName" : { "S" : "GU Today UK" },
            "Status" : { "S" : "Complete" },
            "IsMultipart" : { "BOOL" : false },
            "IsAlwaysOn" : { "BOOL" : true },
            "NumberTargeted" : { "N" : "80" },
            "NumberErrored" : { "N" : "0" },
            "NumberExcluded" : { "N" : "0" },
            "Additional" : { "S" : "EMCNEWEML6619I2_footer" }
          }
     }
    },
EOM
done)

read -r -d '' footer << EOM
  ]
}
EOM
echo "$header" "$body" "$footer" > email-send-output.json