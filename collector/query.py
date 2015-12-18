import datetime
import client
from itertools import imap
from sendresult import SendResult
import dynamodb

import logging
logger = logging.getLogger()

def yesterday():
    return datetime.datetime.now() - datetime.timedelta(days=1)


def daysAgo(days):
    return datetime.datetime.now() - datetime.timedelta(days=days)


"""
Simple Operators
"""


def valueEquals(prop, value):
    return {
        'Property': prop,
        'SimpleOperator': 'equals',
        'Value': value
    }


def dateValueGreaterThan(prop, value):
    return {
        'Property': prop,
        'SimpleOperator': 'greaterThan',
        'DateValue': value
    }


"""
Complex Logic ("complex")
"""


def exactTargetOr(leftOperand, rightOperand):
    return {
        'LeftOperand': leftOperand,
        'RightOperand': rightOperand,
        'LogicalOperator': 'OR'
    }


def exactTargetAnd(leftOperand, rightOperand):
    return {
        'LeftOperand': leftOperand,
        'RightOperand': rightOperand,
        'LogicalOperator': 'AND'
    }


"""
Specific values to query for
"""


def sendWithSendDefinition(customerKey):
    """
    :param customerKey: This is ExternalKey in ExactTarget under; Interactions -> Programs
    :return: A filter to be used when querying for an ExactTarget Send
    """
    return valueEquals('EmailSendDefinition.CustomerKey', customerKey)


def sendDateAfterDateFilter(after):
    """
    :param after: A datetime representing a date in which results will appear after
    :return: A filter to be used when querying ExactTarget Send
    """
    return dateValueGreaterThan('SendDate', after)


def createdDateAfterDateFilter(after):
    return dateValueGreaterThan('CreatedDate', after.isoformat())


def getLastNDaySends(days=1):
    """
    :param days: Last number of days to get
    :return: [SendResult]
    """
    searchFilter = exactTargetAnd(
        exactTargetOr(
            exactTargetOr(
                exactTargetOr(
                    sendWithSendDefinition(111),
                    sendWithSendDefinition(16216)),
                exactTargetOr(
                    sendWithSendDefinition(1933),
                    sendWithSendDefinition(2014))),
            sendWithSendDefinition(16125)),
        sendDateAfterDateFilter(
            daysAgo(days)))

    return imap(
        SendResult.resultToSendResult,
        client.exactTargetSend(searchFilter).get().results)


def getListSubscriberCount():
    response = client.exactTargetList(
        {
            'Property': 'ID',
            'SimpleOperator': 'equals',
            'Value': 3545

        }
    ).get()

    print 'Post Status: ' + str(response.status)
    print 'Code: ' + str(response.code)
    print 'Message: ' + str(response.message)
    print 'Result Count: ' + str(len(response.results))
    print 'Results: ' + str(response.results)


def runCron(event, context):
    """
    :param event: Scheduled event in lambda
    :param context: Context object from lambda
    :return: None
    """
    sends = list(getLastNDaySends(2))
    logger.info('Received event: {0}'.format(event))
    for send in sends:
        logger.info('Putting SEND to dynamo: {0}'.format(send))
        dynamoResponse = dynamodb.putToSendTableWithNowDate(send)
        logger.info('Response from DynamoDB: {0}'.format(dynamoResponse))


def backfillDatabase():
    """
    Backfills the dynamoDB with dates
    :return: None
    """
    sends = list(getLastNDaySends(2))
    print 'Received {0} sends'.format(len(sends))
    for send in sends:
        print 'Putting SEND to dynamo: {0}'.format(send)
        dynamoResponse = dynamodb.putToSendTableWithDate(send, send.sentDate)
        print 'Response from DynamoDB: {0}'.format(dynamoResponse)


def testCredentials(event, context):
    sends = list(getLastNDaySends(2))
    logger.info("Got {0} results".format(len(sends)))
    print "Got {0} results".format(len(sends))
    for send in sends:
        print str(send)
        logger.info(str(send))
