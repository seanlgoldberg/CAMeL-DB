%clear all;

%Constants defined
NUM_AVG=1;
NUM_QUESTIONS = 410;
%NUM_TURKERS = [3,5,9,13];
NUM_TURKERS = 3;
%MEAN_QUALITY = [0,0.2,0.4,0.6,0.8,1.0];
%MEAN_QUALITY = [0.2,0.4,0.6,0.8];
MEAN_QUALITY = 0.5;
STD_QUALITY = 0.3;
%NUM_LABELS_TOT = [2,4];
NUM_LABELS_TOT = 8;
PARAMETER = MEAN_QUALITY;
QUESTIONS_PER_TURK = 1;

%Avg accuracy for DS and MV
comboAns = zeros(NUM_QUESTIONS,NUM_LABELS_TOT,NUM_AVG);
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


for i5=1:numel(NUM_LABELS_TOT),
    for i4=1:numel(MEAN_QUALITY),
        for i3=1:numel(NUM_TURKERS),
            for i2=1:NUM_AVG,
                 
            correct = [];
            incorrect = [];
            
                if (NUM_TURKERS(i3)==1)
                    ONE_TURKER = 1;
                else
                    ONE_TURKER = 0;
                end
                
                %Generate Questions and Answers
                %%[truth, quality, turkerAnswers] = InitializeQuestions(NUM_QUESTIONS, NUM_LABELS_TOT(i5), NUM_TURKERS(i3), MEAN_QUALITY(i4), STD_QUALITY, QUESTIONS_PER_TURK);
                
                %Use AMT provided Answers
                %csvfile = 'RequestedFieldsWithMarginals.csv';
                csvfile = 'SunnyAMT_numeric.csv';
                [truth, quality, turkerAnswers, marg] = convertAMT(csvfile);
                
                %Use EM to predict Turker quality
                %qualityPredict = DawidSkene(turkerAnswers, QUESTIONS_PER_TURK, NUM_TURKERS(i3), NUM_LABELS_TOT(i5));
                
                turkerAnswers = turkerAnswers + 1;
                truth = truth + 1;
                shuffle = randperm(NUM_TURKERS(i3));
                turkerAnswers = turkerAnswers(:,shuffle);
                quality = quality(:,shuffle);
                
                % Majority Vote
                %weighted = zeros(size(turkerAnswers,1),NUM_LABELS_TOT);
                majorityVote = mode(turkerAnswers,2);
%                 for i=1:size(turkerAnswers,2),
%                     for j=1:size(turkerAnswers,1),
%                         weighted(j,(turkerAnswers(j,i))) = weighted(j,(turkerAnswers(j,i))) + quality(j,i);
%                     end
%                 end
%                 [~,wMV] = max(weighted,[],2);
                %wMV = wMV - 1;
                
                %%Bayesian
                %comboBayes = Bayesian(NUM_LABELS_TOT(i5), quality, turkerAnswers, marg);
                comboBayes = Bayesian(NUM_LABELS_TOT(i5), quality, turkerAnswers);
                
                %%Dempster-Shafer
                %Map to mass function
                [mass, Sets] = Map2Mass(NUM_LABELS_TOT(i5), quality, turkerAnswers);
                %[mass2, Sets2] = Map2Mass(NUM_QUESTIONS, NUM_TURKERS(i3), NUM_LABELS_TOT(i5), qualityPredict, turkerAnswers);
              
                %Perform recombination on mass function
                [NUM_QUESTIONS, NUM_TURKERS] = size(turkerAnswers);
                %[comboAns,uncertainty] = DSCombo(NUM_QUESTIONS, NUM_LABELS_TOT(i5), NUM_TURKERS(i3), mass, Sets, ONE_TURKER);
                [comboAns,uncertainty] = DSCombo(NUM_QUESTIONS, NUM_LABELS_TOT(i5), NUM_TURKERS, mass, Sets, ONE_TURKER);
                %comboAns2 = DSCombo(NUM_QUESTIONS, NUM_LABELS_TOT(i5), NUM_TURKERS(i3), mass2, Sets2, ONE_TURKER);
                
                %Threshold
                comboAns(comboAns==0) = eps;
                comboBayes(comboBayes==0) = eps;
                entropyDS = zeros(size(comboAns,1),1);
                entropyB = zeros(size(comboBayes,1),1);
                for idx=1:numel(entropyDS),
                    entropyDS(idx) = -sum(comboAns(idx,:).*log(comboAns(idx,:)),2);
                    entropyB(idx) = -sum(comboBayes(idx,:).*log(comboBayes(idx,:)),2);
                end
                
            %end      
           %comboAns = mean(comboAns,3);
                %Calculate errors
                [cor_DS,cor_maj, cor_B, RunningAvgCorrect(i2,:), RunningAvgIncorrect(i2,:)] = calcAcc(comboAns,comboBayes, truth, majorityVote);
                
                %ROC generation
                rankedDS = sortrows([comboAns truth entropyDS],NUM_LABELS_TOT+2);
                rankedB = sortrows([comboBayes truth entropyB],NUM_LABELS_TOT+2);
                xRocDS = 1:numel(entropyDS);
                yRocDS = zeros(numel(entropyDS),1);
                xRocB = 1:numel(entropyDS);
                yRocB = zeros(numel(entropyDS),1);
                for idx=1:numel(xRocDS),
                    [yRocDS(idx),cor_maj, yRocB(idx), RunningAvgCorrect(i2,:), RunningAvgIncorrect(i2,:)] = calcAcc(rankedDS(1:idx,1:(end-2)),rankedB(1:idx,1:(end-2)), rankedDS(:,(end-1)), majorityVote);
                end
                
                figure; hold on; plot(xRocDS,yRocDS,'r'); plot(xRocB,yRocB,'b');
                
%                 if (cor_maj>cor_DS)
%                     disp('STOP');
%                 end
                Running1(i2) = cor_DS
                Running2(i2) = cor_maj
                Running3(i2) = cor_B
                %Running4(i2) = cor_Wmaj
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
            %%avg_cor_DS(i4) = mean(Running1)
            %%avg_cor_maj(i4) = mean(Running2)
            %%avg_cor_B(i4) = mean(Running3))
             avg_cor_DS(i3) = Running1(i2)
             avg_cor_maj(i3) = Running2(i2)
             avg_cor_B(i3) = Running3(i2)
             

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
    

figure; hold on; 
plot(PARAMETER,avg_cor_B,'g');
plot(PARAMETER,avg_cor_DS,'r'); 
plot(PARAMETER,avg_cor_maj,'b');
axis([0 1 0 1])
xlabel('Mean Turker Accuracy');
ylabel('Accuracy');
title('Integration Comparison vs. Mean Quality of Turkers');
legend('Bayesian', 'Dempster-Shafer', 'Majority Voting');