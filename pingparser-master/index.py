file_name = "testfile.txt"
print(file_name)
file = open(file_name, "r")
for line in file:
    print(line),
file.close()


file = open("testfile2.txt", "w")

file.write("This is a test")
file.write("To add more lines.")

file.close()

fh = open("testfile.txt", "r")
a = fh.readlines()
print(a)
