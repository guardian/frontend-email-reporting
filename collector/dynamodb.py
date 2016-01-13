import datetime
import boto.dynamodb
from config import EmailStatsTableName

dynamoDB = boto.dynamodb.connect_to_region(region_name='eu-west-1')

sendTable = dynamoDB.get_table(EmailStatsTableName)


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
