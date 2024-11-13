

file_path = "OL.cedge.txt"

infile = open(file_path, 'r')
outfile = open(f"preprocessed_{file_path}", 'w')

max_node_id = -1
nodes = set()

for line in infile.readlines():
    tokens = line.split(' ')
    start_node_id = int(tokens[1])
    end_node_id = int(tokens[2])
    if start_node_id > max_node_id:
        max_node_id = start_node_id
    if end_node_id > max_node_id:
        max_node_id = end_node_id
    nodes.add(start_node_id)
    nodes.add(end_node_id)

print(max_node_id)
outfile.write(f"{max_node_id}\n")

infile.close()
infile = open(file_path, 'r')

for line in infile.readlines():
    outfile.write(line)

print(len(nodes))