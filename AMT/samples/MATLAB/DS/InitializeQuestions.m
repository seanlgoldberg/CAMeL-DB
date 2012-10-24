function [gtruth, quality, turkerAnswers] = InitializeQuestions(NUM_QUESTIONS, NUM_LABELS, NUM_TURKERS, MEAN_QUALITY, STD_QUALITY, QUESTIONS_PER_TURK,GTRUTH_FILE, MODEL_TYPE)

% NUM_QUESTIONS       The number of questions to be processed
% NUM_LABELS          The number of possible labels (tags) for each question
% NUM_TUKERS          The number of Turkers answering each individual question
% MEAN_QUALITY        The mean accuracy of the quality generator
% STD_QUALITY         The standard deviation of the quality generator
% QUESTIONS_PER_TURK  The number of questions each specific Turker answers
% GTRUTH_FILE         File to read ground truth from - a CSV
% MODEL_TYPE          The model type used to generate turker answers 
%                     currently 'STANDARD' and 'ONE_LABEL_BIAS'

% SETTING DEFAULT MODEL TYPE
if ( nargin == 7)
    MODEL_TYPE = 'STANDARD';
end

% Total number of Turkers used
ASSIGNED_TURKERS = NUM_QUESTIONS*NUM_TURKERS/QUESTIONS_PER_TURK;
%truth = ceil(rand(NUM_QUESTIONS,1)*NUM_LABELS);
% COLUMN NUMBER OF FILE TO READ GROUND TRUTH VALUES FROM
GTRUTH_COLUMN = 4;
gtruth = dlmread (GTRUTH_FILE,',',[0 GTRUTH_COLUMN NUM_QUESTIONS-1 GTRUTH_COLUMN]);
%answers = zeros(NUM_QUESTIONS,NUM_TURKERS);
quality = zeros(NUM_QUESTIONS,NUM_TURKERS);

% MODELING CONSISTENT BIAS
labelToFlip = floor(rand()*NUM_LABELS);
labelFlippedAs = floor(rand()*NUM_LABELS);
while (labelFlippedAs == labelToFlip )
      labelFlippedAs = floor(rand()*NUM_LABELS);
end

%{
for i=1:NUM_QUESTIONS, %questions
    for j=1:NUM_TURKERS,
        %ensure number is thresholded between 0 and 1
        while(quality(i,j)>1 || quality(i,j)<0)
            quality(i,j) = randn()*STD_QUALITY + MEAN_QUALITY;
        end
        u = rand();
        if (u < quality(i,j)),
            answers(i,j) = truth(i);
        else
            answers(i,j) = ceil(rand()*NUM_LABELS);
        end
    end
end
%}

% Generate qualities for 60 Turkers
turkerQuality = randn(1,ASSIGNED_TURKERS)*STD_QUALITY + MEAN_QUALITY;
% Keep Turker quality between [0,1]
turkerQuality(turkerQuality>1) = 1;
turkerQuality(turkerQuality<0) = 0;

% Generate answers based on Qualities
turkerAnswers = zeros(NUM_QUESTIONS, NUM_TURKERS);
Tcount = 1;
for i=1:NUM_QUESTIONS,
    for j=1:NUM_TURKERS,
        u = rand();
        if (u < turkerQuality(Tcount))
            turkerAnswers(i,j) = gtruth(i);
        else
                turkerAnswers(i,j) = floor(rand()*NUM_LABELS);
               if (STRCMP(MODEL_TYPE,'ONE_LABEL_BIAS'))
                    if (turkerAnswers(i,j)==labelToFlip)
                        if (rand() > MEAN_QUALITY)
                            turkerAnswers(i,j)=labelFlippedAs;
                        end
                    end
               end
        end
        quality(i,j) = turkerQuality(Tcount);
        if (Tcount == ceil(i/QUESTIONS_PER_TURK)*NUM_TURKERS)
            Tcount = ceil(i/QUESTIONS_PER_TURK)*NUM_TURKERS - (NUM_TURKERS-1);
        else
            Tcount = Tcount + 1;
        end
    end
end
csvwrite('syntheticWorkload.csv',turkerAnswers);    