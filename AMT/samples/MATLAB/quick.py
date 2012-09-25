AMTanswerReader = csv.reader(open('responseLog_new.csv', 'rb'), delimiter=',')
answer_rows = []
for i in range(270):
    row = AMTanswerReader.next()
    answer_rows.append(row)

AMTtruthReader = csv.reader(open('hitLog.csv', 'rb'), delimiter=',')
truth_rows = []
for i in range(90):
    row = AMTtruthReader.next()
    truth_rows.append([row[1],row[len(row)-1]])

#check that questions and answers are properly ordered
for i in range(90):
    for j in range(3):
        if truth_rows[i][0]!=answer_rows[i*3 + j][0]:
            print i

for i in range(90):
    for j in range(3):
        answer_rows[i*3+j][3] = answer_rows[i*3+j][3].rstrip()
        answer_rows[i*3+j].append(truth_rows[i][1])
                        
            
    
