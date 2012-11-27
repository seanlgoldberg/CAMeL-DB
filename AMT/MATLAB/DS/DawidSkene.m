function qualityBlock = DawidSkene(block, QUES_PER_HIT, NUM_TURKERS, SunnyFlag)

NUM_LABELS = 8;
truth = zeros(QUES_PER_HIT,1);

if SunnyFlag==0,
    %Convert answer into matrix for ease of use
    answerBlock = zeros(QUES_PER_HIT,NUM_TURKERS);
    count = 1;
    for i3=1:NUM_TURKERS,
        for i2=1:QUES_PER_HIT,
            answerBlock(i2,i3) = str2double(block{count});
            count = count + 1;
        end
    end
else
    answerBlock = block;
end
qualityBlock = ones(1,NUM_TURKERS);
prevIter = zeros(1,NUM_TURKERS);
while(1)
    
    
    %Expectation
    for i=1:QUES_PER_HIT,
        answerMap = zeros(NUM_LABELS,1);
        for j=1:NUM_TURKERS,
            answerMap(answerBlock(i,j)+1) = answerMap(answerBlock(i,j)+1) + qualityBlock(j);
        end
        [~,truth(i)] = max(answerMap);
    end
    
    %Maximization
    for j=1:NUM_TURKERS,
        correct = 0;
        for i=1:QUES_PER_HIT,
            correct = correct + ((truth(i)-1)==answerBlock(i,j));
        end
        qualityBlock(j) = correct/QUES_PER_HIT;
    end
    
    if ~(sum(sum(ones(size(qualityBlock))-(qualityBlock==prevIter))))
        break;
    else
        prevIter = qualityBlock;
    end
end