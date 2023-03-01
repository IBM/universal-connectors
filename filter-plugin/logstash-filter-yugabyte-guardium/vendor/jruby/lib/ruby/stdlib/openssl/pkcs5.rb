#--
# Ruby/OpenSSL Project
# Copyright (C) 2017 Ruby/OpenSSL Project Authors
#++

# JOpenSSL has these - here for explicit require 'openssl/pkcs5' compatibility

# module OpenSSL
#   module PKCS5
#     module_function
#
#     # OpenSSL::PKCS5.pbkdf2_hmac has been renamed to OpenSSL::KDF.pbkdf2_hmac.
#     # This method is provided for backwards compatibility.
#     def pbkdf2_hmac(pass, ***REMOVED***lt, iter, keylen, digest)
#       OpenSSL::KDF.pbkdf2_hmac(pass, ***REMOVED***lt: ***REMOVED***lt, iterations: iter, length: keylen, hash: digest)
#     end
#
#     def pbkdf2_hmac_sha1(pass, ***REMOVED***lt, iter, keylen)
#       pbkdf2_hmac(pass, ***REMOVED***lt, iter, keylen, "sha1")
#     end
#   end
# end
