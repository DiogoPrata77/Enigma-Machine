from enigma.machine import EnigmaMachine


# setup machine 
# Rotor names       |  Enigma Models
# ----------------------------------------
# I, II, III, IV, V | 	All Wehrmacht models
# VI, VII, VIII     |	Kriegsmarine M3 & M4
# Beta, Gamma       |  Kriegsmarine M4 (with thin reflectors) 
#                     

# Reflector names   |  Enigma Models
# ------------------------------------
# B, C              |  All Wehrmacht models
# B-Thin, C-Thin    |  Kriegsmarine M4 (with Beta & Gamma rotors)
#
# Ring settings
# 3 numbers between [0;25]
# 
# Plugboard settings
# Pairs of letters between A-Z up to 10 pairs
# Ref:https://py-enigma.readthedocs.io/en/latest/reference.html#rotor-table-label


machine = EnigmaMachine.from_key_sheet(
        rotors = 'I II III',
        reflector='B',
        ring_settings=[0, 0, 0],
        plugboard_settings='')
 
# set machine initial starting position
machine.set_display('AAA')
# get machine initial starting position
position = machine.get_display()

# cipher/decrypt the message key
msg_key = machine.process_text('AAA', replace_char='X') # replace_char serve para substituir os caracteres especiais por X

print(msg_key)
ciphertext = 'NIBLFMYMLLUFWCASCSSNVHAZ'
plaintext = machine.process_text(ciphertext)



