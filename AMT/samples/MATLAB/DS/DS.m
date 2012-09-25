%clear all;

%Constants defined
NUM_AVG=5;
NUM_QUESTIONS = 200;
%NUM_TURKERS = [1,2,3,4,5,6,7,8,9,10];
NUM_TURKERS = 3;
%MEAN_QUALITY = [0,0.2,0.4,0.6,0.8,1.0];
MEAN_QUALITY = 0.6;
STD_QUALITY = 0.1;
NUM_LABELS_TOT = [2,4,6,8,10,12,14,16,18,20];
%NUM_LABELS_TOT = 10;
PARAMETER = NUM_LABELS_TOT;
QUESTIONS_PER_TURK = 10;

%Avg accuracy for DS and MV
avg_cor_DS = zeros(numel(PARAMETER),1);
avg_cor_maj = zeros(numel(PARAMETER),1);
Running1 = zeros(1,NUM_AVG);
Running2 = zeros(1,NUM_AVG);
RunningAvgCorrect = zeros(NUM_AVG,2);
RunningAvgIncorrect = zeros(NUM_AVG,2);

for i5=1:numel(NUM_LABELS_TOT),
    for i4=1:numel(MEAN_QUALITY),
        for i3=1:numel(NUM_TURKERS),
            for i2=1:NUM_AVG,
                
                if (NUM_TURKERS(i3)==1)
                    ONE_TURKER = 1;
                else
                    ONE_TURKER = 0;
                end
                
                %Generate Questions and Answers
                [truth, quality, turkerAnswers] = InitializeQuestions(NUM_QUESTIONS, NUM_LABELS_TOT(i5), NUM_TURKERS(i3), MEAN_QUALITY(i4), STD_QUALITY, QUESTIONS_PER_TURK);
                
                %Use EM to predict Turker quality
                %qualityPredict = DawidSkene(turkerAnswers, QUESTIONS_PER_TURK, NUM_TURKERS(i3), NUM_LABELS_TOT(i5));
                
                % Majority Vote
                majorityVote = mode(turkerAnswers,2);
                   
                %%Dempster-Shafer
                %Map to mass function
                [mass, Sets] = Map2Mass(NUM_QUESTIONS, NUM_TURKERS(i3), NUM_LABELS_TOT(i5), quality, turkerAnswers);
                
                %Perform recombination on mass function
                comboAns = DSCombo(NUM_QUESTIONS, NUM_LABELS_TOT(i5), NUM_TURKERS(i3), mass,Sets, ONE_TURKER);
                
                %Calculate errors
                [avg_cor_DS(i5),avg_cor_maj(i5), RunningAvgCorrect(i2,:), RunningAvgIncorrect(i2,:)] = calcAcc(NUM_QUESTIONS,comboAns, truth, majorityVote)
                Running1(i2) = avg_cor_DS(i5);
                Running2(i2) = avg_cor_maj(i5);
            end
            avg_cor_DS(i5) = mean(Running1);
            avg_cor_maj(i5) = mean(Running2);
        end    
    end
end
figure; hold on; plot(PARAMETER,avg_cor_DS,'r'); plot(PARAMETER,avg_cor_maj,'b');