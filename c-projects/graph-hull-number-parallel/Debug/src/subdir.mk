################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CU_SRCS += \
../src/vectorAdd.cu 

OBJS += \
./src/vectorAdd.o 

CU_DEPS += \
./src/vectorAdd.d 


# Each subdirectory must supply rules for building sources it contributes
src/%.o: ../src/%.cu
	@echo 'Building file: $<'
	@echo 'Invoking: NVCC Compiler'
	/usr/local/cuda-8.0/bin/nvcc -I"/usr/local/cuda-8.0/samples/0_Simple" -I"/usr/local/cuda-8.0/samples/common/inc" -I"/media/dados/workspace/graph-hull-number-parallel" -G -g -O0 -gencode arch=compute_50,code=sm_50  -odir "src" -M -o "$(@:%.o=%.d)" "$<"
	/usr/local/cuda-8.0/bin/nvcc -I"/usr/local/cuda-8.0/samples/0_Simple" -I"/usr/local/cuda-8.0/samples/common/inc" -I"/media/dados/workspace/graph-hull-number-parallel" -G -g -O0 --compile --relocatable-device-code=false -gencode arch=compute_50,code=compute_50 -gencode arch=compute_50,code=sm_50  -x cu -o  "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


