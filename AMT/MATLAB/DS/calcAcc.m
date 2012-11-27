function [avg_cor_DS,avg_cor_maj, avg_cor_B, RunningAvgCorrect, RunningAvgIncorrect] = calcAcc(comboAns, comboBayes, truth, majorityVote)

NUM_QUESTIONS = size(comboAns,1);
correctDS = zeros(NUM_QUESTIONS,1);
correctMaj = zeros(NUM_QUESTIONS,1);
correctB = zeros(NUM_QUESTIONS,1);
RunningAvgCorrect = [0,0];
RunningAvgIncorrect = [0,0];
%Confusion = zeros(2);
for i=1:NUM_QUESTIONS,
    cur = comboAns(i,:);
    [answ,in] = max(cur);
    if (in==truth(i)),
        correctDS(i) = 1;
        %correctDS(i) = answ;
    else
        correctDS(i) = 0;
        %correctDS(i) = -answ;
    end
    if (majorityVote(i)==truth(i)),
        correctMaj(i) = 1;
        %RunningAvgCorrect(1) = RunningAvgCorrect(1) + (correctMaj(i) - correctDS(i));
    else
        correctMaj(i) = 0;
        %correctMaj(i) = -1;
        %RunningAvgIncorrect(1) = RunningAvgIncorrect(1) + (correctDS(i) - correctMaj(i));
    end
 
end


for i=1:NUM_QUESTIONS,
    curB = comboBayes(i,:);
    [answ,in] = max(curB);
    if (in==truth(i)),
        correctB(i) = 1;
        %correctDS(i) = answ;
    else
        correctB(i) = 0;
        %correctDS(i) = -answ;
    end
end
% RunningAvgCorrect(1) = RunningAvgCorrect(1)/numel(correctMaj(correctMaj==1));
% RunningAvgCorrect(2) = numel(correctMaj(correctMaj==1));
% RunningAvgIncorrect(1) = RunningAvgIncorrect(1)/numel(correctMaj(correctMaj==0));
% RunningAvgIncorrect(2) = numel(correctMaj(correctMaj==0));
%{
for i=1:NUM_QUESTIONS,
    if (correctMaj(i)==1 && correctDS(i)>0)
        Confusion(1,1) = Confusion(1,1) + 1;
    elseif (correctMaj(i)==1 && correctDS(i)<0)
        Confusion(1,2) = Confusion(1,2) + 1;
    elseif (correctMaj(i)==-1 && correctDS(i)>0)
        Confusion(2,1) = Confusion(2,1) + 1;
    elseif (correctMaj(i)==-1 && correctDS(i)<0)
        Confusion(2,2) = Confusion(2,2) + 1;
    end
end

Confusion = Confusion;
%figure; hold on; plot(1:NUM_QUESTIONS,correctDS,'ro');  plot(1:NUM_QUESTIONS,correctMaj,'b*'); hold off;
%}
avg_cor_DS = sum(correctDS)/numel(correctDS);
avg_cor_maj = sum(correctMaj)/numel(correctMaj);
avg_cor_B = sum(correctB)/numel(correctB);
%avg_cor_Wmaj = sum(correctWmaj)/numel(correctWmaj);