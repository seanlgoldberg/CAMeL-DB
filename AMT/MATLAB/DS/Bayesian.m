function comboBayes = Bayesian(NUM_LABELS, quality, turkerAnswers, marg)



[NUM_QUESTIONS, NUM_TURKERS] = size(turkerAnswers);

comboBayes = zeros(NUM_QUESTIONS,NUM_LABELS);
for i=1:NUM_QUESTIONS,
    if (nargin<4),
        P = ones(1,NUM_LABELS);
    else
        P = marg{i};
    end
    for j=1:NUM_LABELS,
        binary_quality = turkerAnswers(i,:);
        binary_quality(binary_quality~=j)=0;
        binary_quality(binary_quality>0)=1;
        for k=1:NUM_TURKERS,
            if binary_quality(k)==1,
                P(j) = P(j)*(quality(i,k) + (1-quality(i,k))*(1/NUM_LABELS));
            else
                P(j) = P(j)*(1-quality(i,k))*(1/NUM_LABELS);
            end
        end
    end
    P = P./sum(P);
    comboBayes(i,:) = P;
end