
from passlib.apps import custom_app_context as pwd_context
from datastore import Datastore

class User(object):
    @staticmethod
    def create():
        return User('', '', {})

    @staticmethod
    def fromName(username):
        user = Datastore.userdata.get(username)
        if user:
            return User(
                name=username,
                pwd=user.get('pwd'),
                filters=user.get('filters')
                )
        return None

    def __init__(self, name, pwd, filters):
        self.name = name
        self.pwd = pwd
        self.filters = filters
        return

    def set_password(self, password):
        self.pwd = pwd_context.encrypt(password)
        return

    def verify_password(self, password):
        return pwd_context.verify(password, self.pwd)

    def commit(self):
        Datastore.userdata.put(self.name, self.dump())
        return

    def to_json(self):
        """
        Public, that is, client-facing data of the user
        """
        return {
            'name': self.name,
            'filters': self.filters
        }

    def dump(self):
        """
        All data of the user
        WARNING: This is meant for debugging only: it exposes the hashed-password.
        """
        dump = self.to_json()
        dump['pwd'] = self.pwd
        return dump
