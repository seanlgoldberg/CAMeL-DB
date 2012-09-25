NUM_TURKERS = 15;
MEAN_QUAL = 0.8;
NUM_LABELS = 2;
truth = floor(rand(10000,1)*NUM_LABELS);
answers = zeros(10000,NUM_TURKERS);
quality = ones(10000,NUM_TURKERS)*(-1);

for i=1:10000, %questions
    for j=1:NUM_TURKERS,
        %ensure number is thresholded between 0 and 1
        while(quality(i,j)>1 || quality(i,j)<0)
            quality(i,j) = randn() + MEAN_QUAL;
        end
        u = rand();
        if (u < quality(i,j)),
            answers(i,j) = truth(i);
        else
            answers(i,j) = floor(rand()*NUM_LABELS);
        end
    end
end
    