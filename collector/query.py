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

