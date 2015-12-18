import query
import dynamodb

import logging
logger = logging.getLogger()


def runCron(event, context):
    """
    :param event: Scheduled event in lambda
    :param context: Context object from lambda
    :return: None
    """
    sends = list(query.getLastNDaySends(2))
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
    sends = list(query.getLastNDaySends(2))
    print 'Received {0} sends'.format(len(sends))
    for send in sends:
        print 'Putting SEND to dynamo: {0}'.format(send)
        dynamoResponse = dynamodb.putToSendTableWithDate(send, send.sentDate)
        print 'Response from DynamoDB: {0}'.format(dynamoResponse)


def testCredentials(event, context):
    sends = list(query.getLastNDaySends(2))
    logger.info("Got {0} results".format(len(sends)))
    print "Got {0} results".format(len(sends))
    for send in sends:
        print str(send)
        logger.info(str(send))

