from util.validate import Validator
from util.validate import ValidatorFactory as Factory
from util.apiexceptions import BadType

# Decide if we want to support the broad types,
# or the specific list, dict types
# from collections import Iterable, Mapping

class BasicTypeValidator(Validator):
    # String used to describe the custom type validated
    label = None

    """
    Base class for custom validators
    """
    def __init__(self, fieldName, forbidNone=False):
        super(BasicTypeValidator, self).__init__()
        self.fieldName = fieldName
        self.forbidNone = forbidNone

    def test(self, valueToTest):
        pass

    def _testForNone(self, valueToTest):
        isNone = False
        if valueToTest is None:
            isNone = True
            if self.forbidNone:
                self._raiseError()
        return isNone

    def _raiseError(self):
        raise BadType('', self.fieldName, self.label)

class ListValidator(BasicTypeValidator):
    label = 'list'
    def __init__(self, fieldName, forbidNone=False):
        super(ListValidator, self).__init__(fieldName, forbidNone)

    def test(self, valueToTest):
        if not super(ListValidator, self)._testForNone(valueToTest):
            if not isinstance(valueToTest, list):
                self._raiseError()
        return valueToTest

Factory.register(ListValidator)

class DictValidator(BasicTypeValidator):
    label = 'dict'
    def __init__(self, fieldName, forbidNone=False):
        super(DictValidator, self).__init__(fieldName, forbidNone)

    def test(self, valueToTest):
        if not super(DictValidator, self)._testForNone(valueToTest):
            if not isinstance(valueToTest, dict):
                self._raiseError()
        return valueToTest

Factory.register(DictValidator)

class IntegerValidator(BasicTypeValidator):
    label = 'int'
    def __init__(self, fieldName, forbidNone=False):
        super(IntegerValidator, self).__init__(fieldName, forbidNone)

    def test(self, valueToTest):
        if not super(IntegerValidator, self)._testForNone(valueToTest):
            try:
                # Convert to float first so that floats and float-strings are treated the same
                # By default, int(float) is valid but int(float_string) is not
                valueToTest = int(float(valueToTest))
                return valueToTest
            except (ValueError, TypeError):
                self._raiseError()

Factory.register(IntegerValidator)

class FloatValidator(BasicTypeValidator):
    label = 'float'
    def __init__(self, fieldName, forbidNone=False):
        super(FloatValidator, self).__init__(fieldName, forbidNone)

    def test(self, valueToTest):
        if not super(FloatValidator, self)._testForNone(valueToTest):
            try:
                # Convert to float first so that floats and float-strings are treated the same
                # By default, int(float) is valid but int(float_string) is not
                valueToTest = float(valueToTest)
                return valueToTest
            except (ValueError, TypeError):
                self._raiseError()

Factory.register(FloatValidator)

class BooleanValidator(BasicTypeValidator):
    label = 'bool'
    def __init__(self, fieldName, forbidNone=False):
        super(BooleanValidator, self).__init__(fieldName, forbidNone)

    def test(self, valueToTest):
        if not super(BooleanValidator, self)._testForNone(valueToTest):
            try:
                valueToTest = bool(valueToTest)
                return valueToTest
            except (ValueError, TypeError):
                self._raiseError()

Factory.register(BooleanValidator)

class StringValidator(BasicTypeValidator):
    label = 'string'
    def __init__(self, fieldName, forbidNone=False):
        super(StringValidator, self).__init__(fieldName, forbidNone)

    def test(self, valueToTest):
        if not super(StringValidator, self)._testForNone(valueToTest):
            try:
                valueToTest = str(valueToTest)
                return valueToTest
            except (ValueError, TypeError):
                self._raiseError()

Factory.register(StringValidator)