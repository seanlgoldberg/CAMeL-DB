% Produce plot showing accuracy vs. # of questions

score = zeros(11,1);
score2 = zeros(11,1);
X = zeros(11,1);
% 500 Questions
X(1) = 0;
score(1) = mean(topk(1:5000,1));
score2(1) = mean(topk(5001:end,1));
for i=1:9,
    X(i+1) = 500*i;
    score(i+1) = mean([topk(1:500*i,2);topk((500*i+1):5000,1)]);
    score2(i+1) = mean([topk(5001:(5000+(500*i)),2);topk((5000+(500*i+1)):end,1)]);
end
X(11) = 5000;
score(11) = mean(topk(1:5000,2));
score2(11) = mean(topk(5001:end,2));

score3 = zeros(11,1);
score4 = zeros(11,1);
%X = zeros(11,1);
% 500 Questions
%X(1) = 0;
score3(1) = mean(topk_method0(1:5000,1));
score4(1) = mean(topk_method0(5001:end,1));
for i=1:9,
    %X(i+1) = 500*i;
    score3(i+1) = mean([topk_method0(1:500*i,2);topk_method0((500*i+1):5000,1)]);
    score4(i+1) = mean([topk_method0(5001:(5000+(500*i)),2);topk_method0((5000+(500*i+1)):end,1)]);
end
%X(11) = 5000;
score3(11) = mean(topk_method0(1:5000,2));
score4(11) = mean(topk_method0(5001:end,2));

figure;
hold on;
plot(X,score,'b');
plot(X,score2,'r');
plot(X,score3,'g');
plot(X,score4,'c');
hold off;