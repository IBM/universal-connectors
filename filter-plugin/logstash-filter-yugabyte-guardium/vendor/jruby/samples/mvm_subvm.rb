require 'jruby/vm'

VM = JRuby::VM
ID = JRuby::VM_ID

# get the VM id of the parent
parent = VM.get_mes***REMOVED***ge

# get the VM id we're sending to
other = VM.get_mes***REMOVED***ge

VM.send_mes***REMOVED***ge(parent,
  "VM #{ID} starting up, sibling is: #{other}, parent is: #{parent}")

# loop until we receive nil, adding one and sending on
while mes***REMOVED***ge = VM.get_mes***REMOVED***ge
  break if mes***REMOVED***ge == "done"
  sleep 0.5
  new_mes***REMOVED***ge = mes***REMOVED***ge + 1
  VM.send_mes***REMOVED***ge(parent, 
    "VM #{JRuby::VM_ID} received: #{mes***REMOVED***ge}, sending #{new_mes***REMOVED***ge}")
  VM.send_mes***REMOVED***ge(other, mes***REMOVED***ge + 1)
end
VM.send_mes***REMOVED***ge(parent, 
  "VM #{JRuby::VM_ID} terminating")