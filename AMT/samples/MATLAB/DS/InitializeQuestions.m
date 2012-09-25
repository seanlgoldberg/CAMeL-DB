function [truth, quality, turkerAnswers] = InitializeQuestions(NUM_QUESTIONS, NUM_LABELS, NUM_TURKERS, MEAN_QUALITY, STD_QUALITY, QUESTIONS_PER_TURK)

ASSIGNED_TURKERS = NUM_QUESTIONS*NUM_TURKERS/QUESTIONS_PER_TURK;
truth = ceil(rand(NUM_QUESTIONS,1)*NUM_LABELS);
%answers = zeros(NUM_QUESTIONS,NUM_TURKERS);
quality = zeros(NUM_QUESTIONS,NUM_TURKERS);

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

%Generate qualities for 60 Turkers
turkerQuality = randn(1,ASSIGNED_TURKERS)*STD_QUALITY + MEAN_QUALITY;
turkerQuality(turkerQuality>1) = 1;
turkerQuality(turkerQuality<0) = 0;

turkerAnswers = zeros(NUM_QUESTIONS, NUM_TURKERS);
Tcount = 1;
for i=1:NUM_QUESTIONS,
    for j=1:NUM_TURKERS,
        u = rand();
        if (u < turkerQuality(Tcount))
            turkerAnswers(i,j) = truth(i);
        else
            turkerAnswers(i,j) = ceil(rand()*NUM_LABELS);
        end
        quality(i,j) = turkerQuality(Tcount);
        if (Tcount == ceil(i/QUESTIONS_PER_TURK)*NUM_TURKERS)
            Tcount = ceil(i/QUESTIONS_PER_TURK)*NUM_TURKERS - (NUM_TURKERS-1);
        else
            Tcount = Tcount + 1;
        end
    end
end
    