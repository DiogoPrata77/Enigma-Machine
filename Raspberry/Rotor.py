from enum import Enum

class Rotor_location(Enum):
	LEFT   = 'left'
	MIDDLE = 'middle'
	RIGHT  = 'right'
# Construção do Rotor
class Rotor():
	@staticmethod
	def create_rotor( rotor_location, config, pos):
		rotor_name = 'rotor_' + rotor_location
		rotor = { rotor_name: {} }
		rotor[rotor_name]['config']   = config
		rotor[rotor_name]['position'] = pos

		return rotor
	
	@staticmethod
	def append_rotors( rotor1, rotor2, rotor3):
		rotors = {}
		rotors.update( rotor1 )
		rotors.update( rotor2 )
		rotors.update( rotor3 )

		return rotors

	@staticmethod
	def append_rotors_and_message( rotors, msg_enc): 
		rotor_msg_append = rotors.copy()
		msg_enc_dict = { 'encrypted_msg' : msg_enc}
		rotor_msg_append.update( msg_enc_dict )

		return rotor_msg_append
