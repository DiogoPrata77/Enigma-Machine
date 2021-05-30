import serial
import time
import firebase_admin
from firebase_admin import credentials,db
from enigma.machine import EnigmaMachine
from Rotor import Rotor, Rotor_location

# Estabelecer coneção com o FireBase
def init_connection_firebase():
	cred = credentials.Certificate('firebase_sdk.json')	
	msg = ""
	try:
		firebase_admin.initialize_app(cred, {
		'databaseURL' : 'https://enigma-machine-se-2021.firebaseio.com/' })
		ref = db.reference('/')
		msg = "Info : Successfully establish a connection to Firebase" 	
	except Exception as exception:
		msg = "Error : Failed to access Firebase"
	return ref, msg

def add_msg_to_android_child_firebase(ref, new_msg):
	try:
		ref_new_child = ref.child( 'msg_to_android' )
		ref_new_child.push( new_msg )
		print(f'Info : Successfully add new message.')
	except Exception as exception:
		print(f'{exception}')
		print(f'Error : Failed to add new message.')
	
def add_msg_to_raspberry_child_firebase(ref, new_msg):
	try:
		ref_new_child = ref.child( 'msg_to_raspberry' )
		ref_new_child.push( new_msg )
		print(f'Info : Successfully add new message.')
	except Exception as exception:
		print(f'Error : Failed to add new message.')
		
def delete_msg_to_android_child_firebase(ref, key):
	try:
		ref_remove_child = ref.child( 'msg_to_android' ).child( key )
		ref_remove_child.delete()
		print(f'Info : Successfully removed message. msg_key = {key}')
	except Exception as exception:
		print(f'Error : Failed to remove message. msg_key = {key}')	

def delete_msg_to_raspberry_child_firebase(ref, key):
	try:
		ref_remove_child = ref.child( 'msg_to_raspberry' ).child( key )
		
		ref_remove_child.delete()
		print(f'Info : Successfully removed message. msg_key = {key}')
	except Exception as exception:
		print(f'Error : Failed to remove message. msg_key = {key}')

def get_last_msg_from_android_child_firebase(ref):
	last_msg = ''
	try:
		ref_msg_to_android = ref.child('msg_to_android')
		all_msgs = ref_msg_to_android.get(etag=False)
		
		if all_msgs is None:	
			print(f'Info : Empty table msg_to_android')	
			return ''
		
		keys     = list(all_msgs.keys())
		last_key = keys[ len(keys) -1]
		last_msg = all_msgs[ last_key ]

		for key in keys:
			delete_msg_to_android_child_firebase(ref, key)

		print(f'Info : Successfully get messages from msg_to_android')		
	except Exception as exception:
		print(f'Error : Failed to get messages from msg_to_android')
	
	return last_msg

def get_last_msg_from_raspberry_child_firebase(ref):
	last_msg = ''
	try:
		ref_msg_to_raspberry = ref.child('msg_to_raspberry')
		all_msgs = ref_msg_to_raspberry.get(etag=False)
		
		if all_msgs is None:	
			print(f'Info : Empty table msg_to_raspberry')	
			return ''

		keys     = list(all_msgs.keys())
		last_key = keys[ len(keys) -1]
		last_msg = all_msgs[ last_key ]

		for key in keys:
			delete_msg_to_raspberry_child_firebase(ref, key)

		print(f'Info : Successfully get messages from msg_to_raspberry')	

	except Exception as exception:
		print(f'Error : Failed to get messages from msg_to_raspberry')
		return ''
	return decrypt_msg(last_msg)


def configure_enigma_machine(rotors_pos, rotor_start_conf):
	machine = EnigmaMachine.from_key_sheet(
		rotors = rotors_pos,
		reflector='B',
		ring_settings=[0, 0, 0],
		plugboard_settings='')

	machine.set_display( rotor_start_conf )

	return machine

def decrypt_msg( msg ):
	rotor_left   = msg['rotor_left']['config']
	rotor_middle = msg['rotor_middle']['config']
	rotor_right  = msg['rotor_right']['config']

	rotor_left_conf   = msg['rotor_left']['position']
	rotor_middle_conf = msg['rotor_middle']['position']
	rotor_right_conf  = msg['rotor_right']['position']

	rotors_pos       = rotor_left + " " + rotor_middle + " " + rotor_right
	rotor_start_conf = rotor_left_conf + rotor_middle_conf + rotor_right_conf

	msg_enc = msg['encrypted_msg']

	machine = configure_enigma_machine( rotors_pos, rotor_start_conf )
	msg = machine.process_text(msg_enc)

	return msg

def cipher_msg( machine, msg):
	#rotor_start_conf = pos1 + pos2 + pos3
	msg_enc = machine.process_text(msg)
	
	return msg_enc

# Atualizar as posições dos Rotors 
def change_representation( key ):
	key_str = ''
	print('Change representation')
	for i in range(len(key)):
		if ( key[i] == '0' ):
			key_str = key_str +  ' I'
		elif( key[i] == '1'):
			key_str = key_str + ' II'
		elif( key[i] == '2'):
			key_str = key_str + ' III'
		elif( key[i] == '3'):
			key_str = key_str + ' IV'
		elif( key[i] == '4'):
			key_str = key_str + ' V'
	return key_str.strip()

def generate_msg( machine, rotors_pos, msg):
	list_rotors = rotors_pos.split(" ") 
	rotor_left   = Rotor.create_rotor( Rotor_location.LEFT.value,   list_rotors[0], machine.get_display()[0])
	rotor_middle = Rotor.create_rotor( Rotor_location.MIDDLE.value, list_rotors[1], machine.get_display()[1])
	rotor_right  = Rotor.create_rotor( Rotor_location.RIGHT.value,  list_rotors[2], machine.get_display()[2])

	all_rotors_dict = Rotor.append_rotors(rotor_left, rotor_middle, rotor_right)	
	msg_enc         = cipher_msg( machine, msg )
	new_msg         = Rotor.append_rotors_and_message( all_rotors_dict, msg_enc)

	return new_msg

def main():
	machine   = None
	rotor_pos = ''
	msg       = ''
	channel   = None

	print("INFO : Starting the communication")
	try:

		channel = serial.Serial('/dev/ttyACM0', 9600, timeout = 0.5) # change name, if needed
		if( channel.isOpen() == False):
			channel.open()
	except serial.serialutil.SerialException as exception:
		print(f'Error: {exception}')
		exit(-1)
	time.sleep(1) # the Arduino is reset after enabling the serial connectio, therefore we have to wait some seconds
	print("INFO : Channel to communicate has started")
	
	ref, msg = init_connection_firebase()

	print( msg )
	if 'Error' in msg:
		exit( -3 )
		
	try:
		while True:
			try:
				response_raw = channel.readline().decode("UTF-8")
			except:
				print("Info : Message received from arduino had noise")
				continue
			list_response = response_raw.split(",")
			print(list_response)
			if( len(list_response) >= 2 ):
				print("INFO : Executing new operation")
				if( list_response[0] == '0'):
					#configure machine
					# ver como vou receber a informação I II III-AAA
					print("INFO : Configuring the enigma machine")
					#print(list_response)
					#print(list_response[1])
					key_raw = list_response[1].strip().split("-")
					#print(key_raw)
					rotor_pos = change_representation(key_raw[0])
					#print(rotor_pos)
					rotor_start_conf = key_raw[1]
					debug_msg= ''
					try:
						machine = configure_enigma_machine(rotor_pos, rotor_start_conf)
						debug_msg = "INFO : Successfully setting enigma machine"
					except:
						debug_msg = "Error : Configuration message was in wrong format"
					finally:
						print(debug_msg)
				elif( list_response[0] == '1' ):
					#send message to android
					print("INFO : Trying to send new message to android")
					msg = list_response[1].strip()

					if( machine == None):
						print("Warning : First step - configure the enigma machine")
						continue

					new_msg = generate_msg( machine, rotor_pos, msg)
					add_msg_to_android_child_firebase(ref, new_msg)
			last_msg = get_last_msg_from_raspberry_child_firebase(ref)
			if (last_msg != ''):
				#add terminator to string
				msg = last_msg + "$"
				print( msg  )
				channel.write(msg.encode())
	except KeyboardInterrupt:
		channel.close()

main()

'''
ref.set({
	'messages':
		{			
			'random_key' : {
				'rotor_left' : {
					'config' : 'I',
					'position': 3
				},
				'rotor_middle': {
					'config': 'IV',
					'position': 5
				},	
				'rotor_right': {
					'config': 'II',
					'position': 23,
				},
				'encrypted_msg': 'ola' }
		}
	})		
'''
