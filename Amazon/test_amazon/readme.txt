bob@Bob:~/AndroidStudioProjects/Amazon/test_amazon$ keytool -genkey -v -keystore keystore.jks -alias androiddebugkey -keyalg RSA -sigalg SHA1withRSA -keysize 2048 -validity 10000
Enter keystore password:  
Re-enter new password: 
What is your first and last name?
  [Unknown]:  Bob
What is the name of your organizational unit?
  [Unknown]:  wistron
What is the name of your organization?
  [Unknown]:  wistron
What is the name of your City or Locality?
  [Unknown]:  suzhou
What is the name of your State or Province?
  [Unknown]:  jiangsu
What is the two-letter country code for this unit?
  [Unknown]:  CN
Is CN=Bob, OU=wistron, O=wistron, L=suzhou, ST=jiangsu, C=CN correct?
  [no]:  y

Generating 2,048 bit RSA key pair and self-signed certificate (SHA1withRSA) with a validity of 10,000 days
	for: CN=Bob, OU=wistron, O=wistron, L=suzhou, ST=jiangsu, C=CN
Enter key password for <androiddebugkey>
	(RETURN if same as keystore password):  
Re-enter new password: 
[Storing keystore.jks]
bob@Bob:~/AndroidStudioProjects/Amazon/test_amazon$ keytool -list -v -alias androiddebugkey -keystore keystore.jks
Enter keystore password:  
Alias name: androiddebugkey
Creation date: Oct 21, 2016
Entry type: PrivateKeyEntry
Certificate chain length: 1
Certificate[1]:
Owner: CN=Bob, OU=wistron, O=wistron, L=suzhou, ST=jiangsu, C=CN
Issuer: CN=Bob, OU=wistron, O=wistron, L=suzhou, ST=jiangsu, C=CN
Serial number: 22780170
Valid from: Fri Oct 21 10:55:33 CST 2016 until: Tue Mar 08 10:55:33 CST 2044
Certificate fingerprints:
	 MD5:  D1:33:AF:A3:64:DB:EE:91:80:26:65:A0:4E:7F:5C:34
	 SHA1: A3:2B:93:E4:86:09:19:0C:B6:32:E1:21:B6:06:D0:69:A5:82:62:A8
	 SHA256: 48:00:F0:09:34:27:21:79:37:F3:28:A5:AF:C3:B7:44:B3:9D:6E:E0:78:80:78:9E:1C:2B:07:DF:EA:7D:63:4C
	 Signature algorithm name: SHA1withRSA
	 Version: 3

Extensions: 

#1: ObjectId: 2.5.29.14 Criticality=false
SubjectKeyIdentifier [
KeyIdentifier [
0000: B1 29 6B 9B F2 4A C9 8C   01 AB D3 DF 79 FF 15 91  .)k..J......y...
0010: C4 C2 E7 96                                        ....
]
]

bob@Bob:~/AndroidStudioProjects/Amazon/test_amazon$ 

