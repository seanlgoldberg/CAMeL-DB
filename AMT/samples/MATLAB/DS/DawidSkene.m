function qualityPredict = DawidSkene(turkerAnswers, QUESTIONS_PER_TURK, NUM_TURKERS, NUM_LABELS)
qualityPredict = zeros(size(turkerAnswers,1)/QUESTIONS_PER_TURK,1);
qual = zeros(1,NUM_TURKERS);

for i=1:size(turkerAnswers,1)/QUESTIONS_PER_TURK,
    block = turkerAnswers((QUESTIONS_PER_TURK*(i-1)+1):(QUESTIONS_PER_TURK*(i-1)+QUESTIONS_PER_TURK),:);
    labels = mode(block,2);
    for m=1:10,
        %Expectation
        for turk = 1:NUM_TURKERS,
            qual(turk) = numel(block(block(:,turk)==labels))/QUESTIONS_PER_TURK;
        end
        
        %Maximization
        for j=1:QUESTIONS_PER_TURK,
            tmp2 = zeros(1,NUM_LABELS);
            for k=1:NUM_TURKERS,
                for n=1:NUM_LABELS
                    if (block(j,k)==n)
                        tmp2(n) = tmp2(n) + qual(k);
                    end
                end
            end
            [~,ind] = max(tmp2);
            labels(j) = ind;
        end
    end
    qualityPredict(i:i+2) = qual;
end
                
                