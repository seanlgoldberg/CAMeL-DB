function [comboAns, uncertainty] = DSCombo(NUM_QUESTIONS,NUM_LABELS,NUM_TURKERS,mass,Sets,ONE_TURKER)

Running = zeros(NUM_QUESTIONS,NUM_LABELS+1);
MASS_SIZE = NUM_LABELS+1;

for i=1:NUM_QUESTIONS,
    
    Q = squeeze(mass(i,:,:));
    for j=1:NUM_TURKERS,
        
        if (ONE_TURKER)
            Q = Q';
        end
        
        if (j==1)
            Running(i,:) = Q(j,:);
        else
            Rsum = zeros(1,MASS_SIZE);
            
            for k=1:MASS_SIZE,
                K = 0;
       
                for k2=1:MASS_SIZE,
                    for k3=1:MASS_SIZE,
                        Outer = Q(j,k2)*Running(i,k3);
                        
                        if Outer<0
                            disp('Error: Outer<0');
                        end
                        
                        if (~isempty(find(intersect(Sets{k2},Sets{k3})==k,1)))
                            Rsum(k) = Rsum(k) + Outer;
                        elseif isempty(intersect(Sets{k2},Sets{k3}))
                            K = K + Outer;
                        end
                        
                        if K<0
                            disp('Error: K>0');
                        
                        end
                    end
                end
                
                if (mean(Rsum(k))==0)
                    Rsum(k) = 0;
                else
                     Rsum(k) = (1/(1-K))*Rsum(k);
                     if K>=1
                        disp('Error: K>1');
                     end
                end
                
               
            end
            N = sum(Rsum,2);
            Running(i,:) = Rsum/N;
        end
    end
end
comboAns = Running;

for i=1:NUM_LABELS,
    comboAns(:,i) = comboAns(:,i) + comboAns(:,NUM_LABELS+1);
end
uncertainty = comboAns(:,NUM_LABELS+1);
N = sum(comboAns(:,1:NUM_LABELS),2);
for i=1:NUM_LABELS,
    comboAns(:,i) = comboAns(:,i)./N;
end
comboAns = comboAns(:,1:NUM_LABELS); % normalized probability distribution