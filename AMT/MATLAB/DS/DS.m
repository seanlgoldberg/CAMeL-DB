%clear all;

%Constants defined
NUM_AVG=1;
NUM_QUESTIONS = 500;
NUM_TURKERS = [3,5,9,13];
%NUM_TURKERS = 5;
%MEAN_QUALITY = [0,0.2,0.4,0.6,0.8,1.0];
%MEAN_QUALITY = [0.25,0.5,0.75];
MEAN_QUALITY = 0.5;
STD_QUALITY = 0.3;
%NUM_LABELS_TOT = [2,4];
NUM_LABELS_TOT = 8;
PARAMETER = MEAN_QUALITY;
QUESTIONS_PER_TURK = 1;

%Avg accuracy for DS and MV
avg_cor_DS = zeros(numel(PARAMETER),1);
avg_cor_maj = zeros(numel(PARAMETER),1);
avg_cor_B = zeros(numel(PARAMETER),1);
avg_cor_Wmaj = zeros(numel(PARAMETER),1);
Running1 = zeros(1,NUM_AVG);
Running2 = zeros(1,NUM_AVG);
Running3 = zeros(1,NUM_AVG);
Running4 = zeros(1,NUM_AVG);
RunningAvgCorrect = zeros(NUM_AVG,2);
RunningAvgIncorrect = zeros(NUM_AVG,2);
weighted = zeros(NUM_QUESTIONS,NUM_LABELS_TOT);

for i5=1:numel(NUM_LABELS_TOT),
    for i4=1:numel(MEAN_QUALITY),
        for i3=1:numel(NUM_TURKERS),
            correct = [];
            incorrect = [];
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
                for i=1:size(turkerAnswers,2),
                    for j=1:size(turkerAnswers,1),
                        weighted(j,turkerAnswers(j,i)) = weighted(j,turkerAnswers(j,i)) + quality(j,i);
                    end
                end
                [~,wMV] = max(weighted,[],2);
                   
                %%Bayesian
                comboBayes = Bayesian(NUM_QUESTIONS, NUM_TURKERS(i3), NUM_LABELS_TOT(i5), quality, turkerAnswers);
                
                %%Dempster-Shafer
                %Map to mass function
                [mass, Sets] = Map2Mass(NUM_QUESTIONS, NUM_TURKERS(i3), NUM_LABELS_TOT(i5), quality, turkerAnswers);
                %[mass2, Sets2] = Map2Mass(NUM_QUESTIONS, NUM_TURKERS(i3), NUM_LABELS_TOT(i5), qualityPredict, turkerAnswers);
                
                %Perform recombination on mass function
                [comboAns,uncertainty] = DSCombo(NUM_QUESTIONS, NUM_LABELS_TOT(i5), NUM_TURKERS(i3), mass, Sets, ONE_TURKER);
                %comboAns2 = DSCombo(NUM_QUESTIONS, NUM_LABELS_TOT(i5), NUM_TURKERS(i3), mass2, Sets2, ONE_TURKER);
                
                %Threshold
                comboAns(comboAns==0) = 0.01;
                entropy = -sum(comboAns.*log(comboAns));
                               
                %Calculate errors
                [cor_DS,cor_maj, cor_B, cor_Wmaj, RunningAvgCorrect(i2,:), RunningAvgIncorrect(i2,:)] = calcAcc(NUM_QUESTIONS,comboAns,comboBayes, truth, majorityVote,wMV);
                
                
                
%                 if (cor_maj>cor_DS)
%                     disp('STOP');
%                 end
                Running1(i2) = cor_DS;
                Running2(i2) = cor_maj;
                Running3(i2) = cor_B;
                Running4(i2) = cor_Wmaj;
                if (cor_DS~=cor_B),
                    disp('STOP');
                end
                
%                 if (cor_DS==1)
%                     correct(end+1) = entropy;
%                     %correct(end+1) = uncertainty;
%                 else
%                     incorrect(end+1) = entropy;
%                     %incorrect(end+1) = uncertainty;
%                 end
%                 correct = sort(correct);
%                 incorrect = sort(incorrect);
                
            end
            %Change parameter here
            avg_cor_DS(i3) = mean(Running1)
            avg_cor_maj(i3) = mean(Running2)
            avg_cor_B(i3) = mean(Running3)
            avg_cor_Wmaj(i3) = mean(Running4)
%             figure; hist(correct,30);
%             figure; hist(incorrect,30);
%             xROC = zeros(numel(correct),1);
%             yROC = zeros(numel(correct),1);
%             for j=1:numel(correct),        
%                     c = incorrect(incorrect<correct(j));
%                     acc = j./(j+numel(c));
%                     xROC(j) = j+numel(c);
%                     yROC(j) = acc;
%             end
%             figure; plot(xROC,yROC);
        end    
    end
end
    

figure; hold on; plot(PARAMETER,avg_cor_DS,'r'); plot(PARAMETER,avg_cor_maj,'b');