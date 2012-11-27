
figure; 
hold on;
%num = 6999;
mappy = containers.Map(-1,[0 0 0 0 0 ]);

clamped1 = load('DBLP_HighEntropy/clamped3.csv');

clamped1 = sortrows(clamped1,5);
c = sortrows(clamped1,4);
% A = sortrows(clamped1,4);
% f = find(A(:,5)==1420);
% f = [f;find(A(:,5)==679)];
% f = [f;find(A(:,5)==244)];
% f = [f;find(A(:,5)==2061)];
% f = [f;find(A(:,5)==243)];
% f = [f;find(A(:,5)==1419)];
% f = [f;find(A(:,5)==1478)];
% f = [f;find(A(:,5)==1421)];
% f = [f;find(A(:,5)==2058)];
% f = [f;find(A(:,5)==2023)];
% c = removerows(A,'ind',f);
clusterID = 1;
c(1,4) = clusterID;
num = size(c,1);
for i=2:num,
    if (c(i,5)~=c(i-1,5))
        clusterID = clusterID + 1;
    end
    c(i,4) = clusterID;
end

numClusters = clusterID;
total1 = zeros(numClusters,1);

for n=1:numClusters,
    index = 1; 
    while (index~=num && c(index,4)<=n),
     index = index + 1;
    end
    clamped = sum(c(1:index,3));
    if (index~=num),
        unclamped = sum(c((index+1):num,2));
    else
        unclamped = 0;
    end
    total1(n) = (clamped +  unclamped)/num;
    
end
plot(1:numClusters,total1,'b');

clamped2 = load('DBLP_HighEntropy/clamped6.csv');

clamped2 = sortrows(clamped2,5);
c = sortrows(clamped2,4);
% A = sortrows(clamped2,4);
% f = find(A(:,5)==1420);
% f = [f;find(A(:,5)==679)];
% f = [f;find(A(:,5)==244)];
% f = [f;find(A(:,5)==2061)];
% f = [f;find(A(:,5)==243)];
% f = [f;find(A(:,5)==1419)];
% f = [f;find(A(:,5)==1478)];
% f = [f;find(A(:,5)==1421)];
% f = [f;find(A(:,5)==2058)];
% f = [f;find(A(:,5)==2023)];
% c = removerows(A,'ind',f);
clusterID = 1;
c(1,4) = clusterID;
num = size(c,1);
for i=2:num,
    if (c(i,5)~=c(i-1,5))
        clusterID = clusterID + 1;
    end
    c(i,4) = clusterID;
end

numClusters = clusterID;
total2 = zeros(numClusters,1);

for n=1:numClusters,
    index = 1; 
    while (index~=num && c(index,4)<=n),
     index = index + 1;
    end
    clamped = sum(c(1:index,3));
    if (index~=num),
        unclamped = sum(c((index+1):num,2));
    else
        unclamped = 0;
    end
    total2(n) = (clamped +  unclamped)/num;
end
plot(1:numClusters,total2,'r');


clamped3 = load('DBLP_HighEntropy/clamped9.csv');

clamped3 = sortrows(clamped3,5);
c = sortrows(clamped3,4);
% A = sortrows(clamped3,4);
% f = find(A(:,5)==1420);
% f = [f;find(A(:,5)==679)];
% f = [f;find(A(:,5)==244)];
% f = [f;find(A(:,5)==2061)];
% f = [f;find(A(:,5)==243)];
% f = [f;find(A(:,5)==1419)];
% f = [f;find(A(:,5)==1478)];
% f = [f;find(A(:,5)==1421)];
% f = [f;find(A(:,5)==2058)];
% f = [f;find(A(:,5)==2023)];
% c = removerows(A,'ind',f);
clusterID = 1;
c(1,4) = clusterID;
num = size(c,1);
for i=2:num,
    if (c(i,5)~=c(i-1,5))
        clusterID = clusterID + 1;
    end
    c(i,4) = clusterID;
end

numClusters = clusterID;
total3 = zeros(numClusters,1);

for n=1:numClusters,
    index = 1; 
    while (index~=num && c(index,4)<=n),
     index = index + 1;
    end
    clamped = sum(c(1:index,3));
    if (index~=num),
        unclamped = sum(c((index+1):num,2));
    else
        unclamped = 0;
    end
    total3(n) = (clamped +  unclamped)/num;
end
plot(1:numClusters,total3,'g');


%clamped4 = load('clamped12.csv');

% A = clamped3;
% f = find(A(:,5)==1420);
% f = [f;find(A(:,5)==679)];
% f = [f;find(A(:,5)==244)];
% f = [f;find(A(:,5)==2061)];
% f = [f;find(A(:,5)==243)];
% f = [f;find(A(:,5)==1419)];
% f = [f;find(A(:,5)==1478)];
% f = [f;find(A(:,5)==1421)];
% f = [f;find(A(:,5)==2058)];
% f = [f;find(A(:,5)==2023)];
% c = removerows(A,'ind',f);
% clusterID = 1;
% c(1,4) = clusterID;
% num = size(c,1);
% for i=2:num,
%     if (c(i,5)~=c(i-1,5))
%         clusterID = clusterID + 1;
%     end
%     c(i,4) = clusterID;
% end
% 
% numClusters = clusterID;
% total4 = zeros(numClusters,1);
% 
% 
% for i=1:num,
%     if mappy.isKey(c(i,4)),
%         m = mappy(c(i,4));
%         mappy(c(i,4)) = [m;c(i,:)];
%     else
%         mappy(c(i,4)) = c(i,:);
%     end
% end
% 
% c=zeros(1,5);
% remove(mappy,-1);
% keys = mappy.keys;
% nn = [];
% for j=1:numel(keys),
%     m = mappy(keys{j});
%     nn(end+1)=size(m,1);
%     c = [c;m];
% end
% 
% for n=1:numClusters,
%     index = 1; 
%     while (index~=num && c(index,4)<=n),
%      index = index + 1;
%     end
%     clamped = sum(c(1:index,3));
%     if (index~=num),
%         unclamped = sum(c((index+1):num,2));
%     else
%         unclamped = 0;
%     end
%     total4(n) = (clamped +  unclamped)/num;
% end
% 
% plot(1:numClusters,total4,'c');

xlabel('# Clusters/Questions Asked');
ylabel('Total Accuracy');
title('Clustering Algorithm: Total Entropy Ranking');
legend('Same Token Neighborhood', 'Same Label Neighborhood', 'Same Field');

%figure; hist(nn,unique(nn));