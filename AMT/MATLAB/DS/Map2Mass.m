function [mass, Sets] = Map2Mass(NUM_LABELS, quality, turkerAnswers)

[NUM_QUESTIONS, NUM_TURKERS] = size(turkerAnswers);
mass = zeros(NUM_QUESTIONS,NUM_TURKERS,NUM_LABELS+1);
full_set = zeros(1,NUM_LABELS);
Sets = cell(1,NUM_LABELS+1);
for single_set=1:NUM_LABELS,
    Sets{single_set} = single_set;
    full_set(single_set) = single_set;
end

Sets{NUM_LABELS+1} = full_set;

for i=1:NUM_QUESTIONS,
    block = turkerAnswers(i,:);
    for k=1:NUM_TURKERS,
        for j=1:NUM_LABELS,
            if (block(k)==j)
                mass(i,k,j) = quality(i,k);
            else
                mass(i,k,j) = 0;
            end
        end
        mass(i,k,NUM_LABELS+1) = 1-quality(i,k);
    end
end