
class SendResult(object):
    @staticmethod
    def resultToSendResult(result):
        return SendResult(
            result.EmailSendDefinition.CustomerKey,
            result.NumberSent,
            result.HardBounces,
            result.SoftBounces,
            result.UniqueClicks,
            result.UniqueOpens,
            result.Unsubscribes,
            result.SendDate,
            result.SentDate,
            result.NumberDelivered,

            result.FromAddress,
            result.FromName,
            result.Duplicates,
            result.InvalidAddresses,
            result.ExistingUndeliverables,
            result.ExistingUnsubscribes,
            result.OtherBounces,
            result.ForwardedEmails,
            result.MissingAddresses,
            result.Subject,
            result.PreviewURL,
            result.EmailName,
            result.Status,
            result.IsMultipart,
            result.IsAlwaysOn,
            result.NumberTargeted,
            result.NumberErrored,
            result.NumberExcluded,
            result.Additional
        )

    def __init__(self,
                 emailSendDefinitionID,
                 numberSent,
                 hardBounces,
                 softBounces,
                 uniqueClicks,
                 uniqueOpens,
                 unsubscribes,
                 sendDate,
                 sentDate,
                 numbererDelivered,
                 fromAddress,
                 fromName,
                 duplicates,
                 invalidAddresses,
                 existingUndeliverables,
                 existingUnsubscribes,
                 otherBounces,
                 forwardedEmails,
                 missingAddresses,
                 subject,
                 previewURL,
                 emailName,
                 status,
                 isMultipart,
                 isAlwaysOn,
                 numberTargeted,
                 numberErrored,
                 numberExcluded,
                 additional
                 ):
        self.emailSendDefinitionID = emailSendDefinitionID
        self.numberSent = numberSent
        self.hardBounces = hardBounces
        self.softBounces = softBounces
        self.uniqueClicks = uniqueClicks
        self.uniqueOpens = uniqueOpens
        self.unsubscribes = unsubscribes
        self.sendDate = sendDate
        self.sentDate = sentDate
        self.numberDelivered = numbererDelivered

        self.fromAddress = fromAddress
        self.fromName = fromName
        self.duplicates = duplicates
        self.invalidAddresses = invalidAddresses
        self.existingUndeliverables = existingUndeliverables
        self.existingUnsubscribes = existingUnsubscribes
        self.otherBounces = otherBounces
        self.forwardedEmails = forwardedEmails
        self.missingAddresses = missingAddresses
        self.subject = subject
        self.previewURL = previewURL
        self.emailName = emailName
        self.status = status
        self.isMultipart = isMultipart
        self.isAlwaysOn = isAlwaysOn
        self.numberTargeted = numberTargeted
        self.numberErrored = numberErrored
        self.numberExcluded = numberExcluded
        self.additional = additional

    def asDict(self):
        return {
            'EmailSendDefinitionID': self.emailSendDefinitionID,
            'NumberSent': self.numberSent,
            'HardBounces': self.hardBounces,
            'SoftBounces': self.softBounces,
            'UniqueClicks': self.uniqueClicks,
            'UniqueOpens': self.uniqueOpens,
            'Unsubscribes': self.unsubscribes,
            'SentDate': self.sentDate.isoformat(),
            'SendDate': self.sendDate.isoformat(),
            'NumberDelivered': self.numberDelivered,
            'FromAddress': self.fromAddress,
            'FromName': self.fromName,
            'Duplicates': self.duplicates,
            'InvalidAddresses': self.invalidAddresses,
            'ExistingUndeliverables': self.existingUndeliverables,
            'ExistingUnsubscribes': self.existingUnsubscribes,
            'OtherBounces': self.otherBounces,
            'ForwardedEmails': self.forwardedEmails,
            'MissingAddresses': self.missingAddresses,
            'Subject': self.subject,
            'PreviewURL': self.previewURL,
            'EmailName': self.emailName,
            'Status': self.status,
            'IsMultipart': True if self.isMultipart else False,
            'IsAlwaysOn': True if self.isAlwaysOn else False,
            'NumberTargeted': self.numberTargeted,
            'NumberErrored': self.numberErrored,
            'NumberExcluded': self.numberExcluded,
            'Additional': self.additional
        }

    def getDurationOfSend(self):
        """
        :rtype: datetime.timedelta
        """
        return self.sentDate - self.sendDate

    def __str__(self):
        return str(self.asDict())
