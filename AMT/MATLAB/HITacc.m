function [correct, answers] = HITacc(data,textdata,dataHIT,textdataHIT)

NUM_QUESTIONS = size(dataHIT,1);
NUM_ANS = size(data,1)/size(dataHIT,1);
correct = 0;
answers = zeros(NUM_QUESTIONS,NUM_ANS);

for i=1:NUM_QUESTIONS*NUM_ANS,
    responseLabel = data(i,1);
    responseConf = data(i,2);
    
    for j=1:NUM_QUESTIONS,
        %Compare HIT ID
        if (textdataHIT{j,2}==textdata{i,1})
            
            if (answers(j,1)==0),
                answers(j,1) = responseLabel;
            elseif (answers(j,2)==0),
                answers(j,2) = responseLabel;
            else
                answers(j,3) = responseLabel;
            end
            if(dataHIT(j,5)==responseLabel)
                correct = correct+1;
            end
        end
    end
end

