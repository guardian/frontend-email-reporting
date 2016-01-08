import datetime
import client
from sendresult import SendResult
from itertools import izip_longest, imap
import config

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


def grouper(iterable, n, fillvalue=None):
    "Collect data into fixed-length chunks or blocks"
    # grouper('ABCDEFG', 3, 'x') --> ABC DEF Gxx
    args = [iter(iterable)] * n
    return izip_longest(fillvalue=fillvalue, *args)


def buildOrsFromList(orList):
    """
    Build a nested OR structure for exact target
    :param orList: list of items to get OR'd
    :return: exactTargetOr structure
    """
    if len(orList) == 2:
        return exactTargetOr(orList[0], orList[1])
    else:
        return buildOrsFromList(
            [exactTargetOr(first, second) if second else first for first, second in grouper(orList, 2)])


def getLastNDaySends(days=1):
    """
    :param days: Last number of days to get
    :return: [SendResult]
    """
    orLists = buildOrsFromList(list(imap(lambda id: sendWithSendDefinition(id), config.SEND_DEFINITIONS)))

    searchFilter = exactTargetAnd(
        orLists,
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

