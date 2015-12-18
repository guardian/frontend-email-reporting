import datetime
import boto.dynamodb
from boto.dynamodb2.table import Table

dynamoDB = boto.dynamodb.connect_to_region(region_name='eu-west-1')
table_name = 'email-send-report-TEST'

sendTable = dynamoDB.get_table(table_name)


def formatDate(date):
    return date.strftime("%Y-%m-%dT%H:%M:%S.000Z")


def putToSendTableWithDate(sendResult, date):
    """
    :param sendResult: SendResult
    :return:
    """
    dateKey = formatDate(date)

    return sendTable.new_item(
            hash_key=sendResult.emailSendDefinitionID,
            range_key=dateKey,
            attrs=sendResult.asDict()).put()


def putToSendTableWithNowDate(sendResult):
    return putToSendTableWithDate(sendResult, datetime.datetime.now())


def listTables():
    return dynamoDB.list_tables()
