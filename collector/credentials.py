import boto.s3.connection
import ConfigParser
import io

credentialsBucket = 'aws-frontend-artifacts'
credentialsSection = 'Web Services'
credentialsKeys = 'lambda/credentials/config.python'

S3Client = boto.s3.connection.S3Connection()

bucket = S3Client.get_bucket(credentialsBucket)


def fileToCredentials(fileLike):
    parser = ConfigParser.ConfigParser()
    parser.readfp(fileLike)

    return {
        'appsignature': parser.get(credentialsSection, 'appsignature'),
        'clientid': parser.get(credentialsSection, 'clientid'),
        'clientsecret': parser.get(credentialsSection, 'clientsecret'),
        'defaultwsdl': parser.get(credentialsSection, 'defaultwsdl'),
        'authenticationurl': parser.get(credentialsSection, 'authenticationurl'),
        'wsdl_file_local_loc': parser.get(credentialsSection, 'wsdl_file_local_loc')
    }


def getFuelCredentials():
    credentialsFile = io.BytesIO(bucket.get_key(credentialsKeys).get_contents_as_string())
    return fileToCredentials(credentialsFile)
