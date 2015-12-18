def additionalOperandToSimpleFilter(auth_stub, operand):
    simpleFilter = auth_stub.soap_client.factory.create('SimpleFilterPart')
    for k, v in additional_operand.items():
        simpleFilter[k] = v
    return simpleFilter

def complexFilter(auth_stub, leftOperand, rightOperand, logicalOperator, additionalOperands):
    complexFilter = auth_stub.soap_client.factory.create('ComplexFilterPart')

    complexFilter["LeftOperand"] = filterFor(auth_stub, leftOperand)
    complexFilter["RightOperand"] = filterFor(auth_stub, rightOperand)
    complexFilter["LogicalOperator"] = logicalOperator

    for op in additionalOperands:
        additionalOperandFilter = additionalOperandToSimpleFilter(op)
        complexFilter.AdditionalOperands.Operand.append(additionalOperandFilter)

    return complexFilter

def simpleFilter(auth_stub, operand):
    simpleFilter = auth_stub.soap_client.factory.create('SimpleFilterPart')
    for prop in simpleFilter:
        propertyKey = prop[0]
        if propertyKey in operand:
            simpleFilter[propertyKey] = operand[propertyKey]

    return simpleFilter


def filterFor(auth_stub, operand):
    result = None
    if operand.has_key('LogicalOperator'):
        result = complexFilter(auth_stub,
                      operand["LeftOperand"],
                      operand["RightOperand"],
                      operand["LogicalOperator"],
                      operand.get('AdditionalOperands', []))
    else:
        result = simpleFilter(auth_stub, operand)

    return result
