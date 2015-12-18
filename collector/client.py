import FuelSDK
import credentials

fuelCredentials = credentials.getFuelCredentials()

fuelSdkClient = FuelSDK.ET_Client(get_server_wsdl=True, debug=False, params=fuelCredentials)


def exactTargetSend(searchFilter=None):
    send = FuelSDK.ET_Send()
    send.auth_stub = fuelSdkClient

    send.search_filter = searchFilter
    # list.props = ['CreatedDate', 'SoftBounces']
    return send


def exactTargetGroup(searchFilter=None):
    send = FuelSDK.ET_Group()
    send.auth_stub = fuelSdkClient

    send.search_filter = searchFilter
    # list.props = ['CreatedDate', 'SoftBounces']
    return send

def exactTargetListSend(searchFilter=None):
    send = FuelSDK.ET_List_Subscriber()
    send.auth_stub = fuelSdkClient

    send.search_filter = searchFilter
    # list.props = ['CreatedDate', 'SoftBounces']
    return send

def exactTargetSubscriberList(searchFilter=None):
    send = FuelSDK.ET_Subscriber_List()
    send.auth_stub = fuelSdkClient

    send.search_filter = searchFilter
    # list.props = ['CreatedDate', 'SoftBounces']
    return send

def exactTargetList(searchFilter=None):
    send = FuelSDK.ET_List()
    send.auth_stub = fuelSdkClient

    send.search_filter = searchFilter
    # list.props = ['CreatedDate', 'SoftBounces']
    return send